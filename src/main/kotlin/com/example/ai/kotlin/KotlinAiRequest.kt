package com.example.ai.kotlin

import com.example.ai.ai.AiRequest
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class KotlinAiRequest(
    @field:NotBlank(message = "message must not be blank")
    @field:Size(max = 10000, message = "message must be at most 10000 characters")
    val message: String? = null,

    @field:Size(max = 2000, message = "systemPrompt must be at most 2000 characters")
    val systemPrompt: String? = null,
) {
    fun toAiRequest(): AiRequest = AiRequest(requireNotNull(message) { "message is required" }, systemPrompt)
}
