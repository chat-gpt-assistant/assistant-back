package com.github.chatgptassistant.assistantback.web.rest

import com.github.chatgptassistant.assistantback.dto.ChatDTO
import com.github.chatgptassistant.assistantback.dto.CreateChatRequest
import com.github.chatgptassistant.assistantback.repository.UserRepository
import com.github.chatgptassistant.assistantback.usecase.ChatUseCase
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
    pageable: Pageable
  ): Page<ChatDTO> {
    val user = (userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found"))

    return chatUseCase.fetchAllChats(user.id, pageable.pageNumber, pageable.pageSize)
      .map { ChatDTO.from(it) }
  }

  @PostMapping
  fun createChat(@RequestHeader("Authorization") userEmail: String,
                 @RequestBody createChatRequest: CreateChatRequest
  ): ChatDTO {
    val user = (userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found"))

    return chatUseCase.createChat(user.id, createChatRequest.title)
      .let { ChatDTO.from(it) }
  }

  @PatchMapping("/{chatId}")
  fun updateChatTitle(
    @RequestHeader("Authorization") userEmail: String,
    @PathVariable chatId: UUID,
    @RequestBody createChatRequest: CreateChatRequest
  ): ChatDTO {
    val user = (userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found"))

    return chatUseCase.updateChatTitle(user.id, chatId, createChatRequest.title)
      .let { ChatDTO.from(it) }
  }

  @DeleteMapping("/{chatId}")
  fun deleteChat(@RequestHeader("Authorization") userEmail: String,
                 @PathVariable chatId: UUID) {
    val user = (userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found"))

    chatUseCase.deleteChat(user.id, chatId)
  }

  @DeleteMapping
  fun deleteAllChats(@RequestHeader("Authorization") userEmail: String) {
    val user = (userRepository.findByEmail(userEmail)
      ?: throw NoSuchElementException("User not found"))

    chatUseCase.deleteAllChats(user.id)
  }

}
