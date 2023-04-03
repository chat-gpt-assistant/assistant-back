package com.github.chatgptassistant.assistantback.web.rest

import com.github.chatgptassistant.assistantback.model.Conversation
import com.github.chatgptassistant.assistantback.model.MessageRequest
import com.github.chatgptassistant.assistantback.repository.UserRepository
import com.github.chatgptassistant.assistantback.service.ConversationService
import kotlinx.coroutines.flow.Flow
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/chats/{chatId}/conversation")
class ConversationResource(
  private val userRepository: UserRepository,
  private val conversationService: ConversationService,
) {

  @GetMapping
  suspend fun getConversation(
    @RequestHeader("Authorization") userEmail: String,
    @PathVariable chatId: UUID,
    @RequestParam(required = false) currentNode: UUID?,
    @RequestParam(required = false, defaultValue = 0.toString()) upperLimit: Int,
    @RequestParam(required = false, defaultValue = 0.toString()) lowerLimit: Int
  ): Conversation {
    userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found") // TODO: replace with real security

    return conversationService.getConversation(chatId, currentNode, upperLimit, lowerLimit)
  }

  // TODO: secure this endpoint, by sending user token
  @GetMapping(path = ["/sse"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
  suspend fun createConnectionAndSendEvents(
    @PathVariable chatId: UUID,
  ): Flow<Conversation> {
    return conversationService.getConversationUpdates(chatId)
  }

  @PostMapping
  suspend fun addMessageToConversation(
    @RequestHeader("Authorization") userEmail: String,
    @PathVariable chatId: UUID,
    @RequestBody messageRequest: MessageRequest
  ): Conversation {
    userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found") // TODO: replace with real security

    return conversationService.addMessageToConversation(chatId, messageRequest)
  }

  @PatchMapping
  suspend fun editConversationMessage(
    @RequestHeader("Authorization") userEmail: String,
    @PathVariable chatId: UUID,
    @RequestParam nodeId: UUID,
    @RequestBody messageRequest: MessageRequest
  ): Conversation {
    userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found") // TODO: replace with real security

    return conversationService.editConversationMessage(chatId, nodeId, messageRequest)
  }

  @PostMapping("/regenerated-response")
  suspend fun regenerateResponseForMessage(
    @RequestHeader("Authorization") userEmail: String,
    @PathVariable chatId: UUID,
  ): Conversation {
    userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found") // TODO: replace with real security

    return conversationService.regenerateResponseForMessage(chatId)
  }

  @PostMapping("/stop-response-generating")
  suspend fun stopResponseGenerating(
    @RequestHeader("Authorization") userEmail: String,
    @PathVariable chatId: UUID,
  ) {
    userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found") // TODO: replace with real security

    conversationService.stopResponseGenerating(chatId)
  }
}
