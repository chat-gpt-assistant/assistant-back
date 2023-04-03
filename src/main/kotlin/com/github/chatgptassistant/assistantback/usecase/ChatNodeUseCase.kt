package com.github.chatgptassistant.assistantback.usecase

import com.github.chatgptassistant.assistantback.domain.ChatNode
import com.github.chatgptassistant.assistantback.domain.Content
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * ChatNode use case.
 */
interface ChatNodeUseCase {
  /**
   * Fetch all chat nodes in a chat.
   *
   * @param chatId chat id
   * @param currentNode current node id. If null, take the chat's current node.
   * @param upperLimit upper limit of messages
   * @param lowerLimit lower limit of messages
   * @return list of chat nodes
   */
  suspend fun fetchAllMessages(chatId: UUID, currentNode: UUID?, upperLimit: Int, lowerLimit: Int): Flow<ChatNode>

  /**
   * Post a message to the chat and generate AI model response.
   *
   * @param chatId chat id
   * @param content message content
   * @return response message
   */
  suspend fun postMessageAndGenerateResponse(chatId: UUID, content: Content): Flow<ChatNode>

  /**
   * Get generated responses.
   * @see postMessageAndGenerateResponse
   * @param chatId chat id
   * @return flow of conversation updates
   */
  fun getGeneratedResponses(chatId: UUID): Flow<ChatNode>

  /**
   * Edit a message and regenerate AI model response.
   *
   * @param chatId chat id
   * @param messageId message id
   * @param newContent new message content
   * @return pair of edited message and response message
   */
  suspend fun editMessageAndRegenerateResponse(
    chatId: UUID,
    messageId: UUID,
    newContent: Content
  ): Flow<ChatNode>

  /**
   * Regenerate AI model response for the last message.
   *
   * @param chatId chat id
   * @return response message
   */
  suspend fun regenerateResponse(chatId: UUID): Flow<ChatNode>

  /**
   * Stop generating AI model response for the last message.
   */
  suspend fun stopResponseGenerating(chatId: UUID)

}

