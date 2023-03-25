package com.github.chatgptassistant.assistantback.domain

import java.time.LocalDateTime
import java.util.*

data class Chat(
  val id: UUID,
  val userId: UUID,
  val title: String,
  val createTime: LocalDateTime,
  val currentNode: UUID? = null,
)
