package com.github.chatgptassistant.assistantback.repository

import com.github.chatgptassistant.assistantback.domain.ChatNode
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface ChatNodeRepository : CoroutineCrudRepository<ChatNode, UUID> {

  suspend fun findByIdAndChatId(id: UUID, chatId: UUID): ChatNode?

  @Query("""{ 'id': { '${'$'}in': ?0 } }""")
  fun findAllById(ids: Iterable<UUID>, sort: Sort): Flow<ChatNode>

  fun findAllByChatIdAndAncestorsContaining(
    chatId: UUID,
    ancestorId: UUID,
    sort: Sort
  ): Flow<ChatNode>

  suspend fun deleteAllByUserId(userId: UUID)
}



