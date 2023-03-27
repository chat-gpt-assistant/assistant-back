package com.github.chatgptassistant.assistantback.repository

import com.github.chatgptassistant.assistantback.domain.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface UserRepository : CoroutineCrudRepository<User, UUID> {
  suspend fun findByEmail(email: String): User?
}
