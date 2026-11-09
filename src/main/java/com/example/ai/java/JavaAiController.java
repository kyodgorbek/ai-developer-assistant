package com.example.ai.java;

import com.example.ai.ai.AiRequest;
import com.example.ai.ai.AiResponse;
import com.example.ai.ai.OllamaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/java/ai")
@RequiredArgsConstructor
public class JavaAiController {

    private final OllamaService ollamaService;

    @PostMapping("/chat")
    public AiResponse chat(@Valid @RequestBody AiRequest request) {
        log.info("Java AI chat request received");
        return ollamaService.chat(request);
    }
}
