package com.github.chatgptassistant.assistantback.web.rest

import com.github.chatgptassistant.assistantback.domain.Chat
import com.github.chatgptassistant.assistantback.dto.CreateChatRequest
import com.github.chatgptassistant.assistantback.repository.UserRepository
import com.github.chatgptassistant.assistantback.usecase.ChatUseCase
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/chats")
class ChatController(
  private val chatUseCase: ChatUseCase,
  private val userRepository: UserRepository, // TODO: temporary solution until we enable authentication
) {
  @GetMapping
  fun fetchAllChats(
    @RequestHeader("Authorization") userEmail: String,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int
  ): Page<Chat> {
    val user = (userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found"))

    return chatUseCase.fetchAllChats(user.id, page, size)
  }

  @PostMapping
  fun createChat(@RequestHeader("Authorization") userEmail: String,
                 @RequestBody createChatRequest: CreateChatRequest
  ): Chat {
    val user = (userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found"))

    return chatUseCase.createChat(user.id, createChatRequest.title)
  }

  @PatchMapping("/{chatId}")
  fun updateChatTitle(
    @RequestHeader("Authorization") userEmail: String,
    @PathVariable chatId: UUID,
    @RequestBody createChatRequest: CreateChatRequest
  ): Chat {
    val user = (userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found"))

    return chatUseCase.updateChatTitle(user.id, chatId, createChatRequest.title)
  }

  @DeleteMapping("/{chatId}")
  fun deleteChat(@RequestHeader("Authorization") userEmail: String,
                 @PathVariable chatId: UUID) {
    val user = (userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found"))

    chatUseCase.deleteChat(user.id, chatId)
  }

  @DeleteMapping
  fun deleteAllChats(@RequestHeader("Authorization") userEmail: String,) {
    val user = (userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found"))

    chatUseCase.deleteAllChats(user.id)
  }

}
