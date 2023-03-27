package com.github.chatgptassistant.assistantback.domain

import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.util.*

//TODO: add token's count to the each Node, so we can know how many tokens we have in the subtree
@Document(collection = "chat_nodes")
data class ChatNode(
  @MongoId
  val id: UUID,
  val userId: UUID,
  val chatId: UUID,
  val parent: UUID? = null,
  val children: List<UUID>,
  @Indexed
  val ancestors: List<UUID>,
  val message: Message
)
