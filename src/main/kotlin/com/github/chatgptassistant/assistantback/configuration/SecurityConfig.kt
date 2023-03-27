package com.github.chatgptassistant.assistantback.configuration;

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.config.WebFluxConfigurerComposite

@Configuration
class SecurityConfig {

  @Bean
  fun corsConfigurer(): WebFluxConfigurer {
    return object : WebFluxConfigurerComposite() {
      override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
          .allowedOriginPatterns("*")
          .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
          .allowedHeaders("*")
          .allowCredentials(true).maxAge(3600)
      }
    }
  }

//  @Bean
//  fun corsConfigurer(): WebMvcConfigurer {
//    return object : WebMvcConfigurer {
//      override fun addCorsMappings(registry: CorsRegistry) {
//        registry.addMapping("/**")
//          .allowedOriginPatterns("*")
//          .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
//          .allowedHeaders("*")
//          .allowCredentials(true).maxAge(3600)
//      }
//    }
//  }
}
