package com.github.chatgptassistant.assistantback.dto

import com.github.chatgptassistant.assistantback.domain.Chat
import com.github.chatgptassistant.assistantback.domain.ChatNode
import java.time.LocalDateTime
import java.util.*

class Conversation(
  val id: UUID,
  val title: String,
  val createTime: LocalDateTime,
  val currentNode: UUID? = null,
  val mapping: Map<UUID, ChatNodeDTO>? = null,
) {
  companion object {
    fun from(chat: Chat, chatNodes: List<ChatNode>): Conversation {
      return Conversation(
        id = chat.id,
        title = chat.title,
        createTime = chat.createTime,
        currentNode = chat.currentNode,
        mapping = chatNodes.map { ChatNodeDTO.from(it) }.associateBy { it.id }
      )
    }
  }
}
