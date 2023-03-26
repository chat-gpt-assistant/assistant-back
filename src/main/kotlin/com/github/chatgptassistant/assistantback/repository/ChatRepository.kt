package com.github.chatgptassistant.assistantback.repository

import com.github.chatgptassistant.assistantback.domain.Chat
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface ChatRepository : MongoRepository<Chat, UUID> {
  fun findAllByUserId(userId: UUID, pageable: Pageable): Page<Chat>
  fun findByIdAndUserId(chatId: UUID, userId: UUID): Chat?
  fun deleteAllByUserId(userId: UUID)
}
