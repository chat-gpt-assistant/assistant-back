package com.github.chatgptassistant.assistantback.infrastructure

import com.github.chatgptassistant.assistantback.service.AIModelInput
import com.github.chatgptassistant.assistantback.service.AIModelResponse
import com.github.chatgptassistant.assistantback.service.AIModelService
import org.springframework.stereotype.Service
import kotlin.math.floor

@Service
class OpenAIModelService: AIModelService {
  override fun complete(input: AIModelInput): AIModelResponse {
    TODO("Not yet implemented")
  }

  override fun getContextLimitInChars(): Int = (4096 * 0.75).toInt() // TODO: fix depending on the model
}
