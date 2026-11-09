package com.example.ai.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KotlinHealthControllerTest {

    @Test
    fun `health returns UP for kotlin`() {
        val response = KotlinHealthController().health()

        assertEquals("UP", response.status)
        assertEquals("kotlin", response.language)
    }
}
