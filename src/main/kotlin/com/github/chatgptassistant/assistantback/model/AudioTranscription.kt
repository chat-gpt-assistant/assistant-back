package com.github.chatgptassistant.assistantback.model

import okio.Source

data class AudioSegment(
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

data class AudioTranscription(
  val text: String,
  val language: String? = null,
  val duration: Double? = null,
  val segments: List<AudioSegment>? = null,
)

data class FileSource(
  val name: String,
  val source: Source,
)
