package com.github.chatgptassistant.assistantback.repository

import com.github.chatgptassistant.assistantback.domain.ChatNode
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.util.*

interface ChatNodeRepository : MongoRepository<ChatNode, UUID> {

  fun findByIdAndChatId(id: UUID, chatId: UUID): ChatNode?

  @Query("""
        {
          'chatId': ?0,
          'ancestors': {
            '${'$'}elemMatch': {
              '${'$'}and': [
                { 'ancestors': { '${'$'}size': { '${'$'}gte': ?1, '${'$'}lte': ?2 } } },
                { '_id': ?3 }
              ]
            }
          }
        }
    """)
  fun findAllByChatIdAndAncestorsContaining(
    chatId: UUID,
    minSize: Int,
    maxSize: Int,
    ancestorId: UUID
  ): List<ChatNode>

  fun findAllByChatIdAndAncestorsContaining(
    chatId: UUID,
    ancestorId: UUID
  ): List<ChatNode>
}



