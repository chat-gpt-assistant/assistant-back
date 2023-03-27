package com.github.chatgptassistant.assistantback.repository

import com.github.chatgptassistant.assistantback.domain.Chat
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface ChatRepository : CoroutineCrudRepository<Chat, UUID> {
  fun findAllByUserId(userId: UUID, pageable: Pageable): Flow<Chat>
  suspend fun findByIdAndUserId(chatId: UUID, userId: UUID): Chat?
  suspend fun deleteAllByUserId(userId: UUID)
}
