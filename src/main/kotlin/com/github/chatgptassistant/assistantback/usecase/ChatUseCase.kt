package com.github.chatgptassistant.assistantback.usecase

import com.github.chatgptassistant.assistantback.domain.Chat
import org.springframework.data.domain.Page
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
  fun createChat(userId: UUID, title: String): Chat

  /**
   * Delete a chat.
   *
   * @param userId user id
   * @param chatId chat id
   */
  fun deleteChat(userId: UUID, chatId: UUID)

  /**
   * Delete all chats for a user.
   *
   * @param userId user id
   */
  fun deleteAllChats(userId: UUID)

  /**
   * Fetch all chats for a user.
   *
   * @param userId user id
   * @param page page number
   * @param size page size
   * @return page of chats
   */
  fun fetchAllChats(userId: UUID, page: Int, size: Int): Page<Chat>

  /**
   * Update chat title.
   *
   * @param userId user id
   * @param chatId chat id
   * @return chat
   */
  fun updateChatTitle(userId: UUID, chatId: UUID, newTitle: String): Chat
}
