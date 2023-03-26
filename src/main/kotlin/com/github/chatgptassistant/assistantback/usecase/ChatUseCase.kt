package com.github.chatgptassistant.assistantback.usecase

import com.github.chatgptassistant.assistantback.domain.Chat
import org.springframework.data.domain.Page
import java.util.*

interface ChatUseCase {
  fun createChat(userId: UUID, title: String): Chat
  fun deleteChat(userId: UUID, chatId: UUID)
  fun deleteAllChats(userId: UUID)
  fun fetchAllChats(userId: UUID, page: Int, size: Int): Page<Chat>
  fun updateChatTitle(userId: UUID, chatId: UUID, newTitle: String): Chat
}
