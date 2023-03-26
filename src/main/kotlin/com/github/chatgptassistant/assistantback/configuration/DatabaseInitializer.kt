package com.github.chatgptassistant.assistantback.configuration

import com.github.chatgptassistant.assistantback.domain.User
import com.github.chatgptassistant.assistantback.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class DatabaseInitializer(
  private val userRepository: UserRepository
) : CommandLineRunner {

  override fun run(vararg args: String?) {
    val user = userRepository.findByEmail("user@user")
    if (user == null) {
      val user = User(
        id = UUID.randomUUID(),
        email = "user@user",
        name = "User",
      )
      userRepository.save(user)
    }
  }

}

