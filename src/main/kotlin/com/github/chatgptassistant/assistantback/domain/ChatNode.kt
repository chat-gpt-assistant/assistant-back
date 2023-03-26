package com.github.chatgptassistant.assistantback.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import java.util.*

//TODO: add token's count to the each Node, so we can know how many tokens we have in the subtree
data class ChatNode(
  @Id
  val id: UUID,
  val chatId: UUID,
  val parent: UUID? = null,
  val children: List<UUID>,
  @Indexed
  val ancestors: List<UUID>,
  val message: Message
)
