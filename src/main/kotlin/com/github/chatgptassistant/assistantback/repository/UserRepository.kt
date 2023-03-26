package com.github.chatgptassistant.assistantback.repository

import com.github.chatgptassistant.assistantback.domain.User
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface UserRepository : MongoRepository<User, UUID> {
  fun findByEmail(email: String): User?
}
