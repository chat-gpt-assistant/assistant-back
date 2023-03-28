package com.github.chatgptassistant.assistantback.event

import AIModelResponseEvent
import com.github.chatgptassistant.assistantback.domain.ChatNode
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class AIModelResponseEventBus {
  private val _eventFlow = MutableSharedFlow<AIModelResponseEvent>(extraBufferCapacity = 10)
  val eventFlow: SharedFlow<AIModelResponseEvent> = _eventFlow

  suspend fun emitEvent(event: AIModelResponseEvent) {
    _eventFlow.emit(event)
  }

  fun createChatNodeFlow(chatId: UUID): Flow<ChatNode> {
    return eventFlow
      .filter { event -> event.chatId == chatId }
      .map { event -> event.chatNode }
  }
}

