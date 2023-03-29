package com.github.chatgptassistant.assistantback.usecase

import com.github.chatgptassistant.assistantback.model.AudioTranscription
import com.github.chatgptassistant.assistantback.model.FileSource

/**
 * Audio use case
 */
interface AudioUseCase {

  /**
   * Transcript audio
   */
  suspend fun transcription(fileSource: FileSource): AudioTranscription
}
