package com.github.chatgptassistant.assistantback.domain

import java.util.*

data class ChatNode(
  val id: UUID,
  val parentId: UUID? = null,
  val children: List<UUID>,
  val message: Message
)
