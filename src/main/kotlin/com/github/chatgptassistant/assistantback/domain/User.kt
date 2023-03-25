package com.github.chatgptassistant.assistantback.domain

import java.util.*

data class User(
  val id: UUID,
  val email: String,
  val name: String,
  val pictureUrl: String? = null
)
