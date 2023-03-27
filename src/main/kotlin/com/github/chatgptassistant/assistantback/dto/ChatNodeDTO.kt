package com.github.chatgptassistant.assistantback.dto

import com.github.chatgptassistant.assistantback.domain.ChatNode
import com.github.chatgptassistant.assistantback.domain.Message
import java.util.*

data class ChatNodeDTO(
  val id: UUID,
  val parent: UUID? = null,
  val children: List<UUID>,
  val message: Message
) {
  companion object {
    fun from(chatNode: ChatNode) = ChatNodeDTO(
      id = chatNode.id,
      parent = chatNode.parent,
      children = chatNode.children,
      message = chatNode.message
    )

  }
}
