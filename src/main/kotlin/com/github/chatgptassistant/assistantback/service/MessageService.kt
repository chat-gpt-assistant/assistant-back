package com.github.chatgptassistant.assistantback.service

import com.github.chatgptassistant.assistantback.domain.*
import com.github.chatgptassistant.assistantback.repository.ChatNodeRepository
import com.github.chatgptassistant.assistantback.repository.ChatRepository
import com.github.chatgptassistant.assistantback.repository.UserRepository
import com.github.chatgptassistant.assistantback.usecase.MessageUseCase
import org.springframework.stereotype.Service
import java.util.*

@Service
class MessageService(
  private val chatRepository: ChatRepository,
  private val chatNodeService: ChatNodeService,
  private val aiModelService: AIModelService,
  private val userRepository: UserRepository,
  private val chatNodeRepository: ChatNodeRepository,
) : MessageUseCase {

  override fun fetchAllMessages(
    userId: UUID,
    chatId: UUID,
    currentNode: UUID?,
    upperLimit: Int,
    lowerLimit: Int
  ): List<ChatNode> {
    val chat = chatRepository.findByIdAndUserId(chatId, userId)
      ?: throw NoSuchElementException("Chat not found")

    val currentNodeId = currentNode ?: chat.currentNode ?: return emptyList()

    return chatNodeService.fetchSubTree(chatId, currentNodeId, upperLimit, lowerLimit)
  }

  override fun postMessageAndGenerateResponse(userId: UUID, chatId: UUID, content: Content): Message {
    val chat = chatRepository.findByIdAndUserId(chatId, userId)
      ?: throw NoSuchElementException("Chat not found")

    val chatNode = chatNodeService.createChatNode(
      chatId = chat.id,
      parentChatNodeId = chat.currentNode,
      message = Message(UUID.randomUUID(), Author.USER, content = content)
    )
    chatRepository.save(chat.copy(currentNode = chatNode.id))

    val aiModelResponses = generateAIModelResponseAndAddToChat(
      chat,
      chatNode.message,
      chatNode
    )

    return aiModelResponses
  }

  override fun editMessageAndRegenerateResponse(
    userId: UUID,
    chatId: UUID,
    messageId: UUID,
    newContent: Content
  ): Pair<Message, Message> {
    val chat = chatRepository.findByIdAndUserId(chatId, userId)
      ?: throw NoSuchElementException("Chat not found")

    val oldMessageNode = chatNodeRepository.findByIdAndChatId(messageId, chatId)
      ?: throw NoSuchElementException("ChatNode not found")

    val newMessageNode = chatNodeService.createChatNode(
      chatId = chatId,
      parentChatNodeId = oldMessageNode.parent,
      message = Message(UUID.randomUUID(), Author.USER, content = newContent)
    )
    chatRepository.save(chat.copy(currentNode = newMessageNode.id))

    val aiModelResponse = generateAIModelResponseAndAddToChat(chat, newMessageNode.message, newMessageNode)

    return oldMessageNode.message to aiModelResponse

  }

  override fun regenerateResponse(userId: UUID, chatId: UUID, messageId: UUID): Message {
    val chat = chatRepository.findByIdAndUserId(chatId, userId)
      ?: throw NoSuchElementException("Chat not found")

    val chatNode = chatNodeRepository.findByIdAndChatId(messageId, chatId)
      ?: throw NoSuchElementException("ChatNode not found")

    val parentNode = chatNodeService.deleteNodeAndDescendants(chatId, chatNode.id)

    return generateAIModelResponseAndAddToChat(chat, chatNode.message, parentNode)
  }

  private fun generateAIModelResponseAndAddToChat(
    chat: Chat,
    message: Message,
    parentChatNode: ChatNode?
  ): Message {
    val ancestorsSize = parentChatNode?.ancestors?.size?.plus(1) ?: 0
    val aiModelInput = buildAIModelInput(chat, message, ancestorsSize)

    val response = aiModelService.complete(aiModelInput)

    val responseContent = Content(type = ContentType.TEXT, parts = listOf(response.choices[0].message.content))
    val responseMessage = Message(UUID.randomUUID(), Author.ASSISTANT, content = responseContent)

    val responseChatNode = chatNodeService.createChatNode(chat.id, parentChatNode?.id, responseMessage)

    chatRepository.save(chat.copy(currentNode = responseChatNode.id))
    return responseMessage
  }

  private fun buildAIModelInput(chat: Chat, message: Message, ancestorsSize: Int): AIModelInput {
    val contextLimitInChars = aiModelService.getContextLimitInChars() / 2 //TODO: fix. Half of the context for input and half for output
    val batchSize = 10

    val messages = mutableListOf<AIModelMessage>()
    var charsCount = 0
    var lastNode = message.id

    for (i in 1..ancestorsSize) {
      val subTree = chatNodeService.fetchSubTree(chat.id, lastNode, batchSize, 0)
      lastNode = subTree.first().id

      subTree
        .map { it.message.content.parts.joinToString(separator = " ") }
        .takeWhile {
          val newCharsCount = charsCount + it.length
          if (newCharsCount > contextLimitInChars) {
            false
          } else {
            charsCount = newCharsCount
            true
          }
        }
        .map { AIModelMessage(role = Role.USER, content = it) }
        .let { messages.addAll(it) }
    }

    return AIModelInput(messages)
  }
}

