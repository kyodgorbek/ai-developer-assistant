package com.example.ai.kotlin

import com.example.ai.ai.AiResponse
import com.example.ai.ai.OllamaService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/kotlin/ai")
class KotlinAiController(private val ollamaService: OllamaService) {

    private val log = LoggerFactory.getLogger(KotlinAiController::class.java)

    @PostMapping("/chat")
    fun chat(@Valid @RequestBody request: KotlinAiRequest): AiResponse {
        log.info("Kotlin AI chat request received")
        return ollamaService.chat(request.toAiRequest())
    }
}
