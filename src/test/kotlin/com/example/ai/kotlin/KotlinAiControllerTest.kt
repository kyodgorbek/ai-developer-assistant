package com.example.ai.kotlin

import com.example.ai.ai.AiRequest
import com.example.ai.ai.AiResponse
import com.example.ai.ai.OllamaException
import com.example.ai.ai.OllamaService
import com.example.ai.ai.OllamaUnavailableException
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(KotlinAiController::class, KotlinHealthController::class)
class KotlinAiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var ollamaService: OllamaService

    @Test
    fun `health endpoint returns UP`() {
        mockMvc.perform(get("/api/v1/kotlin/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.language").value("kotlin"))
    }

    @Test
    fun `chat returns answer from service`() {
        val expected = AiRequest("Explain data classes", null)
        `when`(ollamaService.chat(expected))
            .thenReturn(AiResponse("qwen2.5-coder:3b", "Data classes hold data."))

        mockMvc.perform(
            post("/api/v1/kotlin/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"message":"Explain data classes"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.answer").value("Data classes hold data."))

        verify(ollamaService).chat(expected)
    }

    @Test
    fun `blank message is rejected with 400`() {
        mockMvc.perform(
            post("/api/v1/kotlin/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"message":""}"""),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.details[0]").value("message: message must not be blank"))

        verifyNoInteractions(ollamaService)
    }

    @Test
    fun `missing message is rejected with 400`() {
        mockMvc.perform(
            post("/api/v1/kotlin/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"),
        )
            .andExpect(status().isBadRequest)

        verifyNoInteractions(ollamaService)
    }

    @Test
    fun `ollama down returns 503`() {
        `when`(ollamaService.chat(AiRequest("hi", null)))
            .thenThrow(OllamaUnavailableException("Ollama is not reachable"))

        mockMvc.perform(
            post("/api/v1/kotlin/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"message":"hi"}"""),
        )
            .andExpect(status().isServiceUnavailable)
    }

    @Test
    fun `ollama error returns 502`() {
        `when`(ollamaService.chat(AiRequest("hi", null)))
            .thenThrow(OllamaException("Ollama returned HTTP 500"))

        mockMvc.perform(
            post("/api/v1/kotlin/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"message":"hi"}"""),
        )
            .andExpect(status().isBadGateway)
    }
}
