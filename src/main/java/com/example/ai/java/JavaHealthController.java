package com.example.ai.java;

import com.example.ai.common.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/java")
public class JavaHealthController {

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP", "java");
    }
}
