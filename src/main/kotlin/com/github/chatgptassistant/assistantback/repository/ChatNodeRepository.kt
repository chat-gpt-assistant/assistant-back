package com.github.chatgptassistant.assistantback.repository

import com.github.chatgptassistant.assistantback.domain.ChatNode
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface ChatNodeRepository : MongoRepository<ChatNode, UUID> {

  fun findByIdAndChatId(id: UUID, chatId: UUID): ChatNode?

  fun findAllByChatIdAndAncestorsContaining(
    chatId: UUID,
    ancestorId: UUID
  ): List<ChatNode>

  fun deleteAllByUserId(userId: UUID)
}



