package com.github.chatgptassistant.assistantback.domain

import org.springframework.data.annotation.Id
import java.util.*

data class User(
  @Id
  val id: UUID,
  val email: String,
  val name: String,
  val pictureUrl: String? = null
)
