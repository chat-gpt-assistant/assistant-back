package com.github.chatgptassistant.assistantback.repository

import com.github.chatgptassistant.assistantback.domain.ChatNode
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.util.*

interface ChatNodeRepository : MongoRepository<ChatNode, UUID> {

  fun findByIdAndChatId(id: UUID, chatId: UUID): ChatNode?

  @Query("""{ 'id': { '${'$'}in': ?0 } }""")
  fun findAllById(ids: Iterable<UUID>, sort: Sort): List<ChatNode>

  fun findAllByChatIdAndAncestorsContaining(
    chatId: UUID,
    ancestorId: UUID,
    sort: Sort
  ): List<ChatNode>

  fun deleteAllByUserId(userId: UUID)
}



