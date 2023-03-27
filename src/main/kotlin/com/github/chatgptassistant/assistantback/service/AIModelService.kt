package com.github.chatgptassistant.assistantback.service

import kotlinx.coroutines.flow.Flow

data class AIModelInput(
  val messages: List<AIModelChatDelta>
)

data class AIModelResponse(
  val id: String,
  val created: Int,
  val model: String,
  val choices: List<AIModelChatChunk>,
  val usage: AIModelUsage? = null
)

data class AIModelChatChunk(
  val index: Int? = null,
  val delta: AIModelChatDelta? = null,
  val finishReason: String? = null
)

data class AIModelUsage(
  val promptTokens: Int? = null,
  val completionTokens: Int? = null,
  val totalTokens: Int? = null
)

data class AIModelChatDelta(
  val role: Role? = null,
  val content: String? = null,
  val name: String? = null,
)

data class Role(val role: String) {
  companion object {
    val System: Role = Role("system")
    val User: Role = Role("user")
    val Assistant: Role = Role("assistant")
  }
}

interface AIModelService {

  /**
   * Complete the input
   * @param input the input to complete
   * @return the response
   */
  fun complete(input: AIModelInput): Flow<AIModelResponse>

  /**
   * Get the context limit in chars
   */
  fun getContextLimitInChars(): Int
}
