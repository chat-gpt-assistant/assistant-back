package com.github.chatgptassistant.assistantback.domain

import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.*

data class Chat(
  @Id
  val id: UUID,
  val userId: UUID,
  val title: String,
  val createTime: LocalDateTime = LocalDateTime.now(),
  val currentNode: UUID? = null,
)
