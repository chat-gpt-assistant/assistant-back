package com.github.chatgptassistant.assistantback.service

import kotlinx.coroutines.flow.Flow
import okio.Source

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

data class AIModelFileSource(
  val name: String,
  val source: Source,
)

data class AIModelTranscriptionRequest(
  val audio: AIModelFileSource,
  val model: String,
  val prompt: String,
  val responseFormat: String,
  val temperature: Double,
  val language: String,
)

data class AIModelSegment(
  val id: Int,
  val seek: Int,
  val start: Double,
  val end: Double,
  val text: String,
  val tokens: List<Int>,
  val temperature: Double,
  val avgLogprob: Double,
  val compressionRatio: Double,
  val noSpeechProb: Double,
  val transient: Boolean,
)

data class AIModelTranscription(
  val text: String,
  val language: String? = null,
  val duration: Double? = null,
  val segments: List<AIModelSegment>? = null,
)

interface AIModelService {

  /**
   * Complete the input
   * @param input the input to complete
   * @return the response
   */
  fun complete(input: AIModelInput): Flow<AIModelResponse>

  suspend fun transcription(request: AIModelTranscriptionRequest): AIModelTranscription

  /**
   * Get the context limit in chars
   */
  fun getContextLimitInChars(): Int
}
