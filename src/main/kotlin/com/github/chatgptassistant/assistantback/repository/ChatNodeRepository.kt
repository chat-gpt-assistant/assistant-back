package com.github.chatgptassistant.assistantback.repository

import com.github.chatgptassistant.assistantback.domain.ChatNode
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface ChatNodeRepository : CoroutineCrudRepository<ChatNode, UUID>, ChatNodeCustomRepository {

  suspend fun findByIdAndChatId(id: UUID, chatId: UUID): ChatNode?

  suspend fun deleteAllByUserId(userId: UUID)
}



