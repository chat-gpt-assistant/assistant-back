package com.github.chatgptassistant.assistantback.service

data class AIModelInput(
  val messages: List<AIModelMessage>
)

data class AIModelResponse(
  val id: String,
  val objectName: String,
  val created: Long,
  val model: String,
  val choices: List<AIModelChoice>,
  val usage: AIModelUsage
)

data class AIModelChoice(
  val index: Int,
  val message: AIModelMessage,
  val finishReason: FinishReason
)

data class AIModelUsage(
  val promptTokens: Int,
  val completionTokens: Int,
  val totalTokens: Int
)

data class AIModelMessage(
  val role: Role,
  val content: String
)

enum class FinishReason(val value: String) {
  /**
   * API returned complete model output
   */
  STOP("stop"),

  /**
   * Incomplete model output due to max_tokens parameter or token limit
   */
  LENGTH("length"),

  /**
   * Omitted content due to a flag from our content filters
   */
  CONTENT_FILTER("content_filter"),

  /**
   * API response still in progress or incomplete
   */
  NULL("null"),
}

enum class Role {
  SYSTEM, USER, ASSISTANT
}

interface AIModelService {

  /**
   * Complete the input
   * @param input the input to complete
   * @return the response
   */
  fun complete(input: AIModelInput): AIModelResponse

  /**
   * Get the context limit in chars
   */
  fun getContextLimitInChars(): Int
}
