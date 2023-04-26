package com.github.chatgptassistant.assistantback.service

import AIModelResponseEvent
import com.github.chatgptassistant.assistantback.domain.*
import com.github.chatgptassistant.assistantback.event.AIModelResponseEventBus
import com.github.chatgptassistant.assistantback.repository.ChatNodeRepository
import com.github.chatgptassistant.assistantback.repository.ChatRepository
import com.github.chatgptassistant.assistantback.usecase.ChatNodeUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.util.*

@Service
class ChatNodeService(
  private val chatRepository: ChatRepository,
  private val aiModelService: AIModelService,
  private val chatNodeRepository: ChatNodeRepository,
  private val eventBus: AIModelResponseEventBus
) : ChatNodeUseCase {

  override suspend fun fetchAllMessages(
    chatId: UUID,
    currentNode: UUID?,
    upperLimit: Int,
    lowerLimit: Int
  ): Flow<ChatNode> {
    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    val currentNodeId = currentNode ?: chat.currentNode ?: return emptyList<ChatNode>().asFlow()

    val subTree = chatNodeRepository.fetchSubTree(chatId, currentNodeId, upperLimit, lowerLimit)

    if (currentNode != null) {
      chatRepository.save(chat.copy(currentNode = subTree.last().id))
    }

    return subTree
  }

  override suspend fun postMessageAndGenerateResponse(chatId: UUID, content: Content): Flow<ChatNode> {
    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    return createNewNodeAndGenerateResponse(chat, chat.currentNode, content)
  }

  override fun getGeneratedResponses(chatId: UUID): Flow<ChatNode> {
    return eventBus.createChatNodeFlow(chatId)
  }

  override suspend fun editMessageAndRegenerateResponse(
    chatId: UUID,
    messageId: UUID,
    newContent: Content
  ): Flow<ChatNode> {
    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    val oldMessageNode = chatNodeRepository.findByIdAndChatId(messageId, chatId)
      ?: throw NoSuchElementException("ChatNode not found")

    return createNewNodeAndGenerateResponse(chat, oldMessageNode.parent, newContent)
  }

  private suspend fun createNewNodeAndGenerateResponse(
    chat: Chat,
    parentNodeId: UUID?,
    content: Content
  ): Flow<ChatNode> {
    val (parentNode, chatNode) = chatNodeRepository.createChatNode(
      chat = chat,
      parentChatNodeId = parentNodeId,
      message = Message(UUID.randomUUID(), Author.USER, content = content)
    )

    val updatedChat = chatRepository.save(chat.copy(currentNode = chatNode.id))

    val (updatedChatNode, aiModelResponses) = generateAIModelResponseAndAddToChat(
      updatedChat,
      chatNode.id
    )

    val nodes = mutableListOf(updatedChatNode, aiModelResponses)

    parentNode?.let {
      nodes.add(0, it)
    }

    return nodes.asFlow()
  }

  override suspend fun regenerateResponse(chatId: UUID): Flow<ChatNode> {
    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    val messageId = chat.currentNode
      ?: throw NoSuchElementException("There is not chat node to regenerate response for")

    val chatNode = chatNodeRepository.findByIdAndChatId(messageId, chatId)
      ?: throw NoSuchElementException("ChatNode not found")

    val parentNode = chatNodeRepository.deleteNodeAndDescendants(chatId, chatNode.id)
      ?: throw NoSuchElementException("Parent node not found")

    return generateAIModelResponseAndAddToChat(chat, parentNode.id)
      .toList()
      .asFlow()
  }

  override suspend fun stopResponseGenerating(chatId: UUID) {
    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    val messageId = chat.currentNode ?: throw NoSuchElementException("There is not chat node to stop response for")

    // TODO: wi might have an issue here. Replace with the very next parent node with type of Assistant
    val chatNode = chatNodeRepository.findByIdAndChatId(messageId, chatId)
      ?: throw NoSuchElementException("ChatNode not found")

    chatNodeRepository.save(
      chatNode.copy(
        message = chatNode.message.copy(
          content = chatNode.message.content.copy(final = true)
        )
      )
    )
  }

  /**
   * 1. Create new ChatNode in DB and return it with empty message content instantly
   * 2. Run AIModel in background and update ChatNode with responses
   * @param chat
   * @param parentNodeId
   * @return pair: updated parent to ChatNode with empty message content (following updates will be available in DB)
   */
  private suspend fun generateAIModelResponseAndAddToChat(
    chat: Chat,
    parentNodeId: UUID
  ): Pair<ChatNode, ChatNode> {
    val responseMessage = Message(UUID.randomUUID(), Author.ASSISTANT, content = Content.fromText("", false))
    val createChatNodePair = chatNodeRepository.createChatNode(chat, parentNodeId, responseMessage)
    val parentNode = createChatNodePair.first!!
    val responseChatNode = createChatNodePair.second

    chatRepository.save(chat.copy(currentNode = responseChatNode.id))

    CoroutineScope(Dispatchers.IO).launch {
      val ancestorsSize = parentNode.ancestors.size
      val aiModelInput = buildAIModelInput(chat.id, parentNode.id, ancestorsSize)

      aiModelService.complete(aiModelInput)
        .collect {
          val chunk = it.choices[0]
          val finishReason = chunk.finishReason
          val content = chunk.delta?.content ?: ""

          chatNodeRepository.findById(responseChatNode.id) // TODO: optimisation: update in one request to DB
            ?.let { chatNode ->
              if (chatNode.message.content.final) {
                currentCoroutineContext().cancel()
                return@collect // we were asked to stop generating response
              }

              val parts = chatNode.message.content.parts + content

              val updatedMessage = chatNode.message.copy(
                content = chatNode.message.content.copy(
                  parts = parts,
                  final = finishReason != null
                ),
              )

              val updatedChatNode = chatNodeRepository.save(chatNode.copy(message = updatedMessage))

              eventBus.emitEvent(AIModelResponseEvent(chatId = chat.id, chatNode = updatedChatNode))
            }
        }
    }

    return parentNode to responseChatNode
  }

  private suspend fun buildAIModelInput(chatId: UUID, lastNodeId: UUID, ancestorsSize: Int): AIModelInput {
    val contextLimitInChars =
      aiModelService.getContextLimitInChars() * .7 //TODO: replace with reasonable strategies
    val batchSize = 20

    val messages = mutableListOf<AIModelChatDelta>()
    var charsCount = 0
    var lastNode = lastNodeId

    for (i in 1..ancestorsSize + 1 step batchSize) {
      val subTree = chatNodeRepository.fetchSubTree(chatId, lastNode, batchSize - 1, 0)
        .toList()

      subTree
        .reversed()
        .map {
          AIModelChatDelta(
            role = when (it.message.author) {
              Author.ASSISTANT -> Role.Assistant
              Author.USER -> Role.User
              Author.SYSTEM -> Role.System
            },
            content = it.message.content.parts.joinToString(separator = "")
          )
        }
        .takeWhile {
          val newCharsCount = charsCount + (it.content?.length ?: 0)
          if (newCharsCount > contextLimitInChars) {
            false
          } else {
            charsCount = newCharsCount
            true
          }
        }
        .let { messages.addAll(it) }

      if (charsCount >= contextLimitInChars) {
        break;
      }

      lastNode = subTree.firstOrNull()?.parent ?: break
    }

    return AIModelInput(messages.reversed())
  }
}

