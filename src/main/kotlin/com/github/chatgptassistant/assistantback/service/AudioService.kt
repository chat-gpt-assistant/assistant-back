package com.github.chatgptassistant.assistantback.service

import com.github.chatgptassistant.assistantback.model.AudioTranscription
import com.github.chatgptassistant.assistantback.model.FileSource
import com.github.chatgptassistant.assistantback.usecase.AudioUseCase
import org.springframework.stereotype.Service

@Service
class AudioService(
  private val aiModelService: AIModelService,
): AudioUseCase {
  override suspend fun transcription(fileSource: FileSource): AudioTranscription {
    return aiModelService.transcription(fileSource)
  }
}
