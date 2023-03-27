package com.github.chatgptassistant.assistantback.dto

import com.github.chatgptassistant.assistantback.domain.Chat
import java.time.LocalDateTime
import java.util.*

data class ChatDTO(
  val id: UUID,
  val title: String,
  val createTime: LocalDateTime,
) {
  companion object {
    fun from(chat: Chat): ChatDTO {
      return ChatDTO(
        id = chat.id,
        title = chat.title,
        createTime = chat.createTime,
      )
    }
  }
}
