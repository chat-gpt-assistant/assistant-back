package com.github.chatgptassistant.assistantback.infrastructure

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.github.chatgptassistant.assistantback.service.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@OptIn(BetaOpenAI::class)
@Service
class OpenAIModelService(
  val openAI: OpenAI,
) : AIModelService {
  override fun complete(input: AIModelInput): AIModelResponse {
    val messages = input.messages
    .map {
    ChatMessage(
      role = when (it.role) {
        Role.User -> ChatRole.User
        Role.Assistant -> ChatRole.Assistant
        Role.System -> ChatRole.System
        else -> throw IllegalArgumentException("Unknown role ${it.role}")
      },
      content = it.content ?: "",
    )
  }.toMutableList()

  messages.add(0, ChatMessage(ChatRole.System, """
    You are Bobby, a large language model trained by OpenAI. Answer as concisely as possible. Knowledge cutoff: Sep 2021 Current date: ${LocalDateTime.now()}
  """.trimIndent()))


    val chatCompletionRequest = ChatCompletionRequest(
      model = ModelId("gpt-3.5-turbo"),
      messages = messages
    )

    val completions: Flow<ChatCompletionChunk> = openAI.chatCompletions(chatCompletionRequest)

    return runBlocking {
      return@runBlocking completions.map {
        AIModelResponse(
          id = it.id,
          created = it.created,
          model = it.model.id,
          choices = it.choices.map { choice ->
            AIModelChatChunk(
              index = choice.index,
              delta = AIModelChatDelta(
                role = when (choice.delta?.role) {
                  ChatRole.User -> Role.User
                  ChatRole.Assistant -> Role.Assistant
                  ChatRole.System -> Role.System
                  else -> Role.Assistant
                },
                content = choice.delta?.content,
                name = choice.delta?.name
              ),
              finishReason = choice.finishReason
            )
          },
          usage = AIModelUsage(
            promptTokens = it.usage?.promptTokens,
            completionTokens = it.usage?.completionTokens,
            totalTokens = it.usage?.totalTokens
          )
        )
      }.toList()
        .reduce { acc, aiModelResponse ->
          val combinedContent = (acc.choices[0].delta?.content ?: "") + (aiModelResponse.choices[0].delta?.content ?: "")

          return@reduce acc.copy(
            choices = listOf(
              acc.choices[0].copy(
                delta = acc.choices[0].delta?.copy(content = combinedContent)
              )
            )
          )
        }
    }
  }

  override fun getContextLimitInChars(): Int = (4096 * 0.75).toInt() // TODO: fix depending on the model
}
