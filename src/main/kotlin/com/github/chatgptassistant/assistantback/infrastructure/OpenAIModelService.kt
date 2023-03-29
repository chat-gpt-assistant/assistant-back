package com.github.chatgptassistant.assistantback.infrastructure

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.github.chatgptassistant.assistantback.model.AudioSegment
import com.github.chatgptassistant.assistantback.model.AudioTranscription
import com.github.chatgptassistant.assistantback.service.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@OptIn(BetaOpenAI::class)
@Service
class OpenAIModelService(
  val openAI: OpenAI,
) : AIModelService {

  override fun complete(input: AIModelInput): Flow<AIModelResponse> {
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
      }

    val chatCompletionRequest = ChatCompletionRequest(
      model = ModelId("gpt-3.5-turbo"),
      messages = listOf(
        ChatMessage(
          ChatRole.System,
          """
          You are Bobby, a large language model trained by OpenAI. Answer as concisely as possible.
          Knowledge cutoff: Sep 2021. Current date: ${LocalDateTime.now()}
          """.trimIndent()
        ),
        *messages.toTypedArray()
      )
    )

    val completions: Flow<ChatCompletionChunk> = openAI.chatCompletions(chatCompletionRequest)

    return completions.map {
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
    }
  }

  override suspend fun transcription(fileSource: com.github.chatgptassistant.assistantback.model.FileSource): AudioTranscription {
    val modelRequest = TranscriptionRequest(
      model = ModelId("whisper-1"),
      audio = FileSource(
        name = fileSource.name,
        source = fileSource.source,
      ),
    )

    val transcription = openAI.transcription(modelRequest)

    return AudioTranscription(
      text = transcription.text,
      language = transcription.language,
      duration = transcription.duration,
      segments = transcription.segments?.map {
        AudioSegment(
          id = it.id,
          seek = it.seek,
          start = it.start,
          end = it.end,
          text = it.text,
          tokens = it.tokens,
          temperature = it.temperature,
          avgLogprob = it.avgLogprob,
          compressionRatio = it.compressionRatio,
          noSpeechProb =  it.noSpeechProb,
          transient = it.transient,
        )
      },
    )
  }

  override fun getContextLimitInChars(): Int = 4096 * 5 // TODO: fix depending on the model
}
