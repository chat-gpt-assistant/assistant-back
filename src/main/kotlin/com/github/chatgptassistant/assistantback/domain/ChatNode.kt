package com.github.chatgptassistant.assistantback.domain

import org.springframework.data.annotation.Id
import java.util.*

data class ChatNode(
  @Id
  val id: UUID,
  val chatId: UUID,
  val parent: UUID? = null,
  val children: List<UUID>,
  val ancestors: List<UUID>,
  val message: Message
)
