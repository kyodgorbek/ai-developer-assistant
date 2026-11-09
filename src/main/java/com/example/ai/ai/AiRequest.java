package com.example.ai.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Chat request shared by the Java controller and the Kotlin controller (via conversion).
 *
 * @param message      the user prompt
 * @param systemPrompt optional instruction that sets the assistant's behaviour
 */
public record AiRequest(
        @NotBlank(message = "message must not be blank")
        @Size(max = 10000, message = "message must be at most 10000 characters")
        String message,

        @Size(max = 2000, message = "systemPrompt must be at most 2000 characters")
        String systemPrompt) {
}
