package com.github.chatgptassistant.assistantback.web.rest

import com.github.chatgptassistant.assistantback.model.ChatDTO
import com.github.chatgptassistant.assistantback.model.CreateChatRequest
import com.github.chatgptassistant.assistantback.repository.UserRepository
import com.github.chatgptassistant.assistantback.usecase.ChatUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/chats")
class ChatController(
  private val chatUseCase: ChatUseCase,
  private val userRepository: UserRepository, // TODO: temporary solution until we enable authentication
) {
  @GetMapping
  suspend fun fetchAllChats(
    @RequestHeader("Authorization") userEmail: String,
    @RequestParam(value = "page", defaultValue = "0") page: Int,
    @RequestParam(value = "size", defaultValue = "20") size: Int
  ): Flow<ChatDTO> {
    val user = userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found")

    return chatUseCase.fetchAllChats(user.id, page, size)
      .map { ChatDTO.from(it) }
  }

  @PostMapping
  suspend fun createChat(@RequestHeader("Authorization") userEmail: String,
                         @RequestBody createChatRequest: CreateChatRequest
  ): ChatDTO {
    val user = userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found")

    return chatUseCase.createChat(user.id, createChatRequest.title)
      .let { ChatDTO.from(it) }
  }

  @PatchMapping("/{chatId}")
  suspend fun updateChatTitle(
    @RequestHeader("Authorization") userEmail: String,
    @PathVariable chatId: UUID,
    @RequestBody createChatRequest: CreateChatRequest
  ): ChatDTO {
    val user = userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found")

    return chatUseCase.updateChatTitle(user.id, chatId, createChatRequest.title)
      .let { ChatDTO.from(it) }
  }

  @DeleteMapping("/{chatId}")
  suspend fun deleteChat(@RequestHeader("Authorization") userEmail: String,
                         @PathVariable chatId: UUID) {
    val user = userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found")

    chatUseCase.deleteChat(user.id, chatId)
  }

  @DeleteMapping
  suspend fun deleteAllChats(@RequestHeader("Authorization") userEmail: String) {
    val user = userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found")

    chatUseCase.deleteAllChats(user.id)
  }

}
