package com.github.chatgptassistant.assistantback.repository

import com.github.chatgptassistant.assistantback.domain.ChatNode
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface ChatNodeRepository : MongoRepository<ChatNode, UUID> {

  fun findByIdAndChatId(id: UUID, chatId: UUID): ChatNode?

 /* fun findAllByChatIdAndAncestorsSizeBetweenAndAncestorsContaining(
    chatId: UUID,
    minSize: Int,
    maxSize: Int,
    ancestorId: UUID
  ): List<ChatNode>*/

  fun findAllByChatIdAndAncestorsContaining(
    chatId: UUID,
//    minSize: Int,
//    maxSize: Int,
    ancestorId: UUID
  ): List<ChatNode>
}



