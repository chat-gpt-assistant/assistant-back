package com.github.chatgptassistant.assistantback.configuration

import com.aallam.openai.client.OpenAI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAIConfig(
  val applicationProperties: ApplicationConfig.ApplicationProperties,
) {

  @Bean
  fun openAI() = OpenAI(applicationProperties.openAI.key)
}
