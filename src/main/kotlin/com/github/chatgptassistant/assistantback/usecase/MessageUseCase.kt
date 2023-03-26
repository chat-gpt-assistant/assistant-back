package com.github.chatgptassistant.assistantback.usecase

import com.github.chatgptassistant.assistantback.domain.ChatNode
import com.github.chatgptassistant.assistantback.domain.Content
import com.github.chatgptassistant.assistantback.domain.Message
import java.util.*

interface MessageUseCase {
  fun fetchAllMessages(userId: UUID, chatId: UUID, currentNode: UUID?, upperLimit: Int, lowerLimit: Int): List<ChatNode>
  fun postMessageAndGenerateResponse(userId: UUID, chatId: UUID, content: Content): Message
  fun editMessageAndRegenerateResponse(
    userId: UUID,
    chatId: UUID,
    messageId: UUID,
    newContent: Content
  ): Pair<Message, Message>
  fun regenerateResponse(userId: UUID, chatId: UUID, messageId: UUID): Message

}

