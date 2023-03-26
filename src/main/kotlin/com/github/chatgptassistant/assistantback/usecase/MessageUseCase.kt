package com.github.chatgptassistant.assistantback.usecase

import com.github.chatgptassistant.assistantback.domain.ChatNode
import com.github.chatgptassistant.assistantback.domain.Content
import com.github.chatgptassistant.assistantback.domain.Message
import java.util.*

/**
 * Message use case.
 */
interface MessageUseCase {
  /**
   * Fetch all messages in a chat.
   *
   * @param userId user id
   * @param chatId chat id
   * @param currentNode current node id. If null, take the chat's current node.
   * @param upperLimit upper limit of messages
   * @param lowerLimit lower limit of messages
   * @return list of chat nodes
   */
  fun fetchAllMessages(userId: UUID, chatId: UUID, currentNode: UUID?, upperLimit: Int, lowerLimit: Int): List<ChatNode>

  /**
   * Post a message to the chat and generate AI model response.
   *
   * @param userId user id
   * @param chatId chat id
   * @param content message content
   * @return response message
   */
  fun postMessageAndGenerateResponse(userId: UUID, chatId: UUID, content: Content): Message

  /**
   * Edit a message and regenerate AI model response.
   *
   * @param userId user id
   * @param chatId chat id
   * @param messageId message id
   * @param newContent new message content
   * @return pair of edited message and response message
   */
  fun editMessageAndRegenerateResponse(
    userId: UUID,
    chatId: UUID,
    messageId: UUID,
    newContent: Content
  ): Pair<Message, Message>

  /**
   * Regenerate AI model response for the message.
   *
   * @param userId user id
   * @param chatId chat id
   * @param messageId message id
   * @return response message
   */
  fun regenerateResponse(userId: UUID, chatId: UUID, messageId: UUID): Message

}

