package com.github.chatgptassistant.assistantback.domain

import java.time.LocalDateTime
import java.util.*

enum class Author {
  ASSISTANT, USER, SYSTEM
}

data class Content(
  val type: ContentType,
  val parts: List<String>,
  val final: Boolean = true,
) {
  companion object {
    fun fromText(text: String, final: Boolean = true) = Content(ContentType.TEXT, listOf(text), final)
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
