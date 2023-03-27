package com.github.chatgptassistant.assistantback.service

import com.github.chatgptassistant.assistantback.domain.*
import com.github.chatgptassistant.assistantback.repository.ChatNodeRepository
import com.github.chatgptassistant.assistantback.repository.ChatRepository
import com.github.chatgptassistant.assistantback.usecase.MessageUseCase
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.util.*

@Service
class MessageService(
  private val chatRepository: ChatRepository,
  private val chatNodeService: ChatNodeService,
  private val aiModelService: AIModelService,
  private val chatNodeRepository: ChatNodeRepository,
) : MessageUseCase {

  override suspend fun fetchAllMessages(
    chatId: UUID,
    currentNode: UUID?,
    upperLimit: Int,
    lowerLimit: Int
  ): List<ChatNode> {
    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    val currentNodeId = currentNode ?: chat.currentNode ?: return emptyList()

    return chatNodeService.fetchSubTree(chatId, currentNodeId, upperLimit, lowerLimit)
  }

  override suspend fun postMessageAndGenerateResponse(chatId: UUID, content: Content): List<ChatNode> {
    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    val chatNode = chatNodeService.createChatNode(
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

    return listOf(chatNode, aiModelResponses)
  }

  override suspend fun editMessageAndRegenerateResponse(
    chatId: UUID,
    messageId: UUID,
    newContent: Content
  ): List<ChatNode> {
    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    val oldMessageNode = chatNodeRepository.findByIdAndChatId(messageId, chatId)
      ?: throw NoSuchElementException("ChatNode not found")

    val newMessageNode = chatNodeService.createChatNode(
      chat = chat,
      parentChatNodeId = oldMessageNode.parent,
      message = Message(UUID.randomUUID(), Author.USER, content = newContent)
    )
    chatRepository.save(chat.copy(currentNode = newMessageNode.id))

    val aiModelResponse = generateAIModelResponseAndAddToChat(chat, newMessageNode.message, newMessageNode)

    return listOf(newMessageNode, aiModelResponse)
  }

  override suspend fun regenerateResponse(chatId: UUID, messageId: UUID): ChatNode {
    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    val chatNode = chatNodeRepository.findByIdAndChatId(messageId, chatId)
      ?: throw NoSuchElementException("ChatNode not found")

    val parentNode = chatNodeService.deleteNodeAndDescendants(chatId, chatNode.id)

    return generateAIModelResponseAndAddToChat(chat, chatNode.message, parentNode)
  }

  private suspend fun generateAIModelResponseAndAddToChat(
    chat: Chat,
    message: Message,
    parentChatNode: ChatNode?
  ): ChatNode {
    val ancestorsSize = parentChatNode?.ancestors?.size ?: 0
    val aiModelInput = buildAIModelInput(chat, message, ancestorsSize)

    // when Flow is complete we should store it in DB, but meanwhile we will send chunks to user
    val response = aiModelService.complete(aiModelInput)
      .toList()
      .reduce { acc, aiModelResponse ->
        val combinedContent = (acc.choices[0].delta?.content ?: "") + (aiModelResponse.choices[0].delta?.content ?: "")

        return@reduce acc.copy(
          choices = listOf(
            acc.choices[0].copy(
              delta = acc.choices[0].delta?.copy(content = combinedContent)
            )
          )
        )
      }

    val responseContent = Content(
      type = ContentType.TEXT,
      parts = listOf(response.choices[0].delta!!.content!!)
    )
    val responseMessage = Message(UUID.randomUUID(), Author.ASSISTANT, content = responseContent)

    val responseChatNode = chatNodeService.createChatNode(chat, parentChatNode?.id, responseMessage)

    chatRepository.save(chat.copy(currentNode = responseChatNode.id))


    return responseChatNode
  }

  private suspend fun buildAIModelInput(chat: Chat, message: Message, ancestorsSize: Int): AIModelInput {
    val contextLimitInChars =
      aiModelService.getContextLimitInChars() * .7 //TODO: fix. Half of the context for input and half for output
    val batchSize = 20

    val messages = mutableListOf<AIModelChatDelta>()
    var charsCount = 0
    var lastNode = message.id

    for (i in 1..ancestorsSize + 1 step batchSize) {
      val subTree = chatNodeService.fetchSubTree(chat.id, lastNode, batchSize - 1, 0)

      subTree
        .reversed()
        .map {
          AIModelChatDelta(
            role = when (it.message.author) {
              Author.ASSISTANT -> Role.Assistant
              Author.USER -> Role.User
              Author.SYSTEM -> Role.System
            },
            content = it.message.content.parts.joinToString(separator = " ")
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

