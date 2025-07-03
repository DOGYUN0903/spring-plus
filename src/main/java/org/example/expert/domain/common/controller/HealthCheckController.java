package org.example.expert.domain.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @GetMapping
    public Map<String, String> healthCheck() {
        return Map.of(
                "status", "ok",
                "serverTime", LocalDateTime.now().toString()
        );
    }
}
