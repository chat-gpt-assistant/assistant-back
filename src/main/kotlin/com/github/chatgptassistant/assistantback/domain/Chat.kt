package com.github.chatgptassistant.assistantback.domain

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime
import java.util.*

@Document(collection = "chats")
data class Chat(
  @MongoId
  val id: UUID,
  val userId: UUID,
  val title: String,
  val createTime: LocalDateTime = LocalDateTime.now(),
  val currentNode: UUID? = null,
)
