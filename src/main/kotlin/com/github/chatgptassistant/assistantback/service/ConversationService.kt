package com.github.chatgptassistant.assistantback.service

import com.github.chatgptassistant.assistantback.domain.Content
import com.github.chatgptassistant.assistantback.model.MessageRequest
import com.github.chatgptassistant.assistantback.model.Conversation
import com.github.chatgptassistant.assistantback.repository.ChatRepository
import com.github.chatgptassistant.assistantback.usecase.ChatNodeUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.util.*

@Service
class ConversationService(
  private val chatRepository: ChatRepository,
  private val chatNodeUseCase: ChatNodeUseCase,
) {

  /**
   * Get conversation by chat id.
   * @param chatId chat id
   * @param currentNode current node id
   * @param upperLimit upper limit of messages
   * @param lowerLimit lower limit of messages
   * @return conversation with messages
   */
  suspend fun getConversation(chatId: UUID, currentNode: UUID?, upperLimit: Int, lowerLimit: Int): Conversation {
    val chatNodes = chatNodeUseCase.fetchAllMessages(chatId, currentNode, upperLimit, lowerLimit)

    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    return Conversation.from(chat, chatNodes.toList())
  }

  /**
   * Add message to conversation.
   * @param chatId chat id
   * @param messageRequest message request
   * @return conversation with added message
   */
  suspend fun addMessageToConversation(chatId: UUID, messageRequest: MessageRequest): Conversation {
    val chatNodes = chatNodeUseCase.postMessageAndGenerateResponse(chatId, Content.fromText(messageRequest.content))

    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    return Conversation.from(chat, chatNodes.toList())
  }

  suspend fun getConversationUpdates(chatId: UUID): Flow<Conversation> {
    return chatNodeUseCase.getGeneratedResponses(chatId).map {
      val chat = chatRepository.findById(chatId)
        ?: throw NoSuchElementException("Chat not found")
      Conversation.from(chat, listOf(it))
    }
  }

  /**
   * Edit message in conversation.
   * @param chatId chat id
   * @param nodeId node id
   * @param messageRequest message request
   * @return conversation with edited message
   */
  suspend fun editConversationMessage(chatId: UUID, nodeId: UUID, messageRequest: MessageRequest): Conversation {
    val chatNodes = chatNodeUseCase.editMessageAndRegenerateResponse(
      chatId,
      nodeId,
      Content.fromText(messageRequest.content)
    )

    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    return Conversation.from(chat, chatNodes.toList())
  }

  /**
   * Regenerate response for the last message.
   * @param chatId chat id
   * @return conversation with regenerated response
   */
  suspend fun regenerateResponseForMessage(chatId: UUID): Conversation {
    val chatNode = chatNodeUseCase.regenerateResponse(chatId)

    val chat = chatRepository.findById(chatId)
      ?: throw NoSuchElementException("Chat not found")

    return Conversation.from(chat, chatNode.toList())
  }

  /**
   * Stop generating response for the last message.
   * @param chatId chat id
   */
  suspend fun stopResponseGenerating(chatId: UUID) {
    chatNodeUseCase.stopResponseGenerating(chatId)
  }
}
