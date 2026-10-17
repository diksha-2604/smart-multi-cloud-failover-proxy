package com.smcfp.backend;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class BackendController {

    @GetMapping("/")
    public String home() {
        return "Welcome to AWS Backend";
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "backend", "AWS",
            "status", "UP"
        );
    }

    @PostMapping("/request")
    public Map<String, String> receiveRequest(
            @RequestBody Map<String, String> request) {

        return Map.of(
            "backend", "AWS",
            "status", "Request received",
            "message", request.getOrDefault("message", "No message received")
        );
    }
}