package com.github.chatgptassistant.assistantback.domain

import java.time.LocalDateTime
import java.util.*

enum class Author {
  ASSISTANT, USER, SYSTEM
}

data class Content(
  val type: ContentType,
  val parts: List<String>
) {
  companion object {
    fun fromText(text: String) = Content(ContentType.TEXT, listOf(text))
  }
}

enum class ContentType {
  TEXT
}

data class Message(
  val id: UUID,
  val author: Author,
  val createTime: LocalDateTime = LocalDateTime.now(),
  val content: Content
)
