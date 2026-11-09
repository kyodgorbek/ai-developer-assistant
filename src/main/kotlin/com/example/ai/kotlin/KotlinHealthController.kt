package com.example.ai.kotlin

import com.example.ai.common.HealthResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/kotlin")
class KotlinHealthController {

    @GetMapping("/health")
    fun health(): HealthResponse = HealthResponse("UP", "kotlin")
}
