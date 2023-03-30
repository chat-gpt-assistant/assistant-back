package com.github.chatgptassistant.assistantback.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Configuration
@EnableConfigurationProperties(ApplicationConfig.ApplicationProperties::class)
class ApplicationConfig {

  @Validated
  @ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
  data class ApplicationProperties(
    val openAI: OpenAIProperties,
  ) {
    data class OpenAIProperties(
      val key: String,
    ) {
    }
  }
}
