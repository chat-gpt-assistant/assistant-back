package com.github.chatgptassistant.assistantback.service

import com.github.chatgptassistant.assistantback.domain.Chat
import com.github.chatgptassistant.assistantback.repository.ChatNodeRepository
import com.github.chatgptassistant.assistantback.repository.ChatRepository
import com.github.chatgptassistant.assistantback.repository.UserRepository
import com.github.chatgptassistant.assistantback.usecase.ChatUseCase
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*

@Service
class ChatService(
  private val chatRepository: ChatRepository,
  private val userRepository: UserRepository,
  private val chatNodeRepository: ChatNodeRepository
) : ChatUseCase {

  override fun createChat(userId: UUID, title: String): Chat {
    userRepository.findById(userId)
      .orElseThrow { NoSuchElementException("User not found") }

    val chat = Chat(id = UUID.randomUUID(), userId = userId, title = title)
    return chatRepository.save(chat)
  }

  override fun deleteChat(userId: UUID, chatId: UUID) {
    val chat = chatRepository.findByIdAndUserId(chatId, userId)
      ?: throw NoSuchElementException("Chat not found")

    chatRepository.delete(chat)
  }

  override fun deleteAllChats(userId: UUID) {
    chatNodeRepository.deleteAllByUserId(userId)
    chatRepository.deleteAllByUserId(userId)
  }

  override fun fetchAllChats(userId: UUID, page: Int, size: Int): Page<Chat> {
    val pageable = PageRequest.of(page, size)
    return chatRepository.findAllByUserId(userId, pageable)
  }

  override fun updateChatTitle(userId: UUID, chatId: UUID, newTitle: String): Chat {
    val chat = chatRepository.findByIdAndUserId(chatId, userId)
      ?: throw NoSuchElementException("Chat not found")

    val updatedChat = chat.copy(title = newTitle)
    return chatRepository.save(updatedChat)
  }
}
