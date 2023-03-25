package com.github.chatgptassistant.assistantback.domain

import java.time.LocalDateTime
import java.util.*

enum class Author {
  ASSISTANT, USER
}

data class Content(
  val type: ContentType,
  val parts: List<String>
)

enum class ContentType {
  TEXT
}

data class Message(
  val id: UUID,
  val author: Author,
  val createTime: LocalDateTime,
  val content: Content
)
