package com.github.chatgptassistant.assistantback.infrastructure

import com.github.chatgptassistant.assistantback.service.*
import org.springframework.stereotype.Service

@Service
class OpenAIModelService: AIModelService {
  override fun complete(input: AIModelInput): AIModelResponse {
    // TODO: implement

    return AIModelResponse(
      id = "1",
      objectName = "text",
      created = 0,
      model = "davinci",
      choices = listOf(
        AIModelChoice(
          index = 0,
          finishReason = FinishReason.STOP,
          message = AIModelMessage(
            role = Role.ASSISTANT,
            content = "Hello, I'm ChatGPT Assistant. How can I help you?"
          ),
        )
      ),
      usage = AIModelUsage(
        promptTokens = 0,
        completionTokens = 0,
        totalTokens = 0
      )
    )
  }

  override fun getContextLimitInChars(): Int = (4096 * 0.75).toInt() // TODO: fix depending on the model
}
