package com.github.chatgptassistant.assistantback.web.rest

import com.github.chatgptassistant.assistantback.model.AudioTranscription
import com.github.chatgptassistant.assistantback.model.FileSource
import com.github.chatgptassistant.assistantback.usecase.AudioUseCase
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.reactive.asFlow
import okio.*
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/audio")
class AudioResource(
  private val audioService: AudioUseCase,
) {

  @PostMapping("/transcription", consumes = ["multipart/form-data"])
  suspend fun uploadAudio(
    @RequestPart("audio") audio: FilePart
  ): AudioTranscription? {
    val buffer = Buffer()

    audio.content().asFlow().collect {
      buffer.readFrom(it.asInputStream())
    }

    val fileSource = FileSource(
      name = "audio.webm",
      source = buffer.inputStream().source()
    )

    return audioService.transcription(fileSource)
  }
}
