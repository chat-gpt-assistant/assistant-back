package com.github.chatgptassistant.assistantback

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AssistantBackApplication

fun main(args: Array<String>) {
  runApplication<AssistantBackApplication>(*args)
}
