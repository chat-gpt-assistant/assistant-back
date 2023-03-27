package com.github.chatgptassistant.assistantback.domain

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.util.*

@Document(collection = "users")
data class User(
  @MongoId
  val id: UUID,
  val email: String,
  val name: String,
  val pictureUrl: String? = null
)
