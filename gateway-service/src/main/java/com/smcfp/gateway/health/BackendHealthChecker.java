package com.smcfp.gateway.health;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BackendHealthChecker {

    private final RestTemplate restTemplate = new RestTemplate();

    private BackendHealthStatus status = BackendHealthStatus.UNKNOWN;

    private int consecutiveSuccesses = 0;
    private int consecutiveFailures = 0;

    @Scheduled(fixedRate = 10000)
    public void checkBackendHealth() {

        try {
            String response = restTemplate.getForObject(
                    "http://localhost:8081/health",
                    String.class
            );

            consecutiveSuccesses++;
            consecutiveFailures = 0;

            if (status == BackendHealthStatus.UNHEALTHY) {
                status = BackendHealthStatus.RECOVERING;
                consecutiveSuccesses = 1;
            } else if (consecutiveSuccesses >= 2) {
                status = BackendHealthStatus.HEALTHY;
            }

            System.out.println("Backend A Response: " + response);
            System.out.println("Backend A Status: " + status);

        } catch (Exception e) {

            consecutiveFailures++;
            consecutiveSuccesses = 0;

            if (consecutiveFailures >= 3) {
                status = BackendHealthStatus.UNHEALTHY;
            }

            System.out.println("Backend A Status: " + status);
            System.out.println("Health check failed: " + e.getMessage());
        }
    }
}