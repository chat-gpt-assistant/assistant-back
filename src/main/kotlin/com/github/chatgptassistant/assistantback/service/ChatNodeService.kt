package com.github.chatgptassistant.assistantback.service

import AIModelResponseEvent
import com.github.chatgptassistant.assistantback.domain.*
import com.github.chatgptassistant.assistantback.event.AIModelResponseEventBus
import com.github.chatgptassistant.assistantback.repository.ChatNodeRepository
import com.github.chatgptassistant.assistantback.repository.ChatRepository
import com.github.chatgptassistant.assistantback.usecase.ChatNodeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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

    return subTree.asFlow()
  }

  override suspend fun postMessageAndGenerateResponse(chatId: UUID, content: Content): Flow<ChatNode> {
    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    val chatNode = chatNodeRepository.createChatNode(
      chat = chat,
      parentChatNodeId = chat.currentNode,
      message = Message(UUID.randomUUID(), Author.USER, content = content)
    )
    chatRepository.save(chat.copy(currentNode = chatNode.id))

    val aiModelResponses = generateAIModelResponseAndAddToChat(
      chat,
      chatNode.message,
      chatNode
    )

    return listOf(chatNode, aiModelResponses).asFlow()
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

    val newMessageNode = chatNodeRepository.createChatNode(
      chat = chat,
      parentChatNodeId = oldMessageNode.parent,
      message = Message(UUID.randomUUID(), Author.USER, content = newContent)
    )
    chatRepository.save(chat.copy(currentNode = newMessageNode.id))

    val aiModelResponse = generateAIModelResponseAndAddToChat(chat, newMessageNode.message, newMessageNode)

    return listOf(newMessageNode, aiModelResponse).asFlow()
  }

  // TODO: test
  override suspend fun regenerateResponse(chatId: UUID, messageId: UUID): ChatNode {
    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    val chatNode = chatNodeRepository.findByIdAndChatId(messageId, chatId)
      ?: throw NoSuchElementException("ChatNode not found")

    val parentNode = chatNodeRepository.deleteNodeAndDescendants(chatId, chatNode.id)

    return generateAIModelResponseAndAddToChat(chat, chatNode.message, parentNode)
  }

  // TODO: test
  override suspend fun stopResponseGenerating(chatId: UUID, messageId: UUID) {
    val chatNode = chatNodeRepository.findByIdAndChatId(messageId, chatId)
      ?: throw NoSuchElementException("ChatNode not found")

    chatNodeRepository.save(chatNode.copy(
      message = chatNode.message.copy(
        content = chatNode.message.content.copy(final = true)
      )
    ))
  }

  /**
   * 1. Create new ChatNode in DB and return it with empty message content instantly
   * 2. Run AIModel in background and update ChatNode with responses
   * @param chat
   * @param message
   * @param parentChatNode
   * @return ChatNode with empty message content (following updates will be available in DB)
   */
  private suspend fun generateAIModelResponseAndAddToChat(
    chat: Chat,
    message: Message,
    parentChatNode: ChatNode?
  ): ChatNode {
    val responseMessage = Message(UUID.randomUUID(), Author.ASSISTANT, content = Content.fromText("", false))

    val responseChatNode = chatNodeRepository.createChatNode(chat, parentChatNode?.id, responseMessage)

    chatRepository.save(chat.copy(currentNode = responseChatNode.id))

    CoroutineScope(Dispatchers.IO).launch {
      val ancestorsSize = parentChatNode?.ancestors?.size ?: 0
      val aiModelInput = buildAIModelInput(chat, message, ancestorsSize)

      val response = aiModelService.complete(aiModelInput)
        .onEach {
          val chunk = it.choices[0]
          val finishReason = chunk.finishReason
          val content = chunk.delta?.content ?: ""

          chatNodeRepository.findById(responseChatNode.id) // TODO: optimisation: update in one request to DB
            ?.let { chatNode ->
              if (chatNode.message.content.final) {
                return@onEach // we were asked to stop generating response
              }

              val parts = chatNode.message.content.parts + content

              val updatedMessage = chatNode.message.copy(
                content = chatNode.message.content.copy(
                  parts = parts,
                  final = finishReason != null
                ),
              )

              val updatedChatNode = chatNode.copy(message = updatedMessage)
              chatNodeRepository.save(updatedChatNode)

              eventBus.emitEvent(AIModelResponseEvent(chatId = chat.id, chatNode = updatedChatNode))
            }
        }

      response.collect()
    }

    return responseChatNode
  }

  private suspend fun buildAIModelInput(chat: Chat, message: Message, ancestorsSize: Int): AIModelInput {
    val contextLimitInChars =
      aiModelService.getContextLimitInChars() * .7 //TODO: replace with reasonable strategies
    val batchSize = 20

    val messages = mutableListOf<AIModelChatDelta>()
    var charsCount = 0
    var lastNode = message.id

    for (i in 1..ancestorsSize + 1 step batchSize) {
      val subTree = chatNodeRepository.fetchSubTree(chat.id, lastNode, batchSize - 1, 0)

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

      lastNode = subTree.first().parent ?: break
    }

    return AIModelInput(messages.reversed())
  }
}

