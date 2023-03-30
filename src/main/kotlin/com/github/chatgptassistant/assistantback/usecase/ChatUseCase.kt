package com.github.chatgptassistant.assistantback.usecase

import com.github.chatgptassistant.assistantback.domain.Chat
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Chat use case.
 */
interface ChatUseCase {
  /**
   * Create a new chat.
   *
   * @param userId user id
   * @param title chat title
   * @return created chat
   */
  suspend fun createChat(userId: UUID, title: String): Chat

  /**
   * Delete a chat.
   *
   * @param userId user id
   * @param chatId chat id
   */
  suspend fun deleteChat(userId: UUID, chatId: UUID)

  /**
   * Delete all chats for a user.
   *
   * @param userId user id
   */
  suspend fun deleteAllChats(userId: UUID)

  /**
   * Fetch all chats for a user.
   *
   * @param userId user id
   * @param page page number
   * @param size page size
   * @return page of chats
   */
  fun fetchAllChats(userId: UUID, page: Int, size: Int): Flow<Chat>

  /**
   * Update chat title.
   *
   * @param userId user id
   * @param chatId chat id
   * @return chat
   */
  suspend fun updateChatTitle(userId: UUID, chatId: UUID, newTitle: String): Chat
}
