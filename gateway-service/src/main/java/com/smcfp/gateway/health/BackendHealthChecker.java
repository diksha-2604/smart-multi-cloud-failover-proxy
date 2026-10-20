package com.smcfp.gateway.health;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;


@Component
public class BackendHealthChecker {

    private final MeterRegistry meterRegistry;
    private final RestTemplate restTemplate = new RestTemplate();

    private BackendHealthStatus status = BackendHealthStatus.UNKNOWN;
    private double healthValue = -1;

    private int consecutiveSuccesses = 0;
    private int consecutiveFailures = 0;

    public BackendHealthChecker(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        Gauge.builder("smcfp_backend_health", this, checker ->
                checker.healthValue)
            .description("Backend A health status: 1=healthy, 0=unhealthy, -1=unknown or recovering")
            .register(meterRegistry);
    }

    

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
            healthValue = (status == BackendHealthStatus.HEALTHY) ? 1 : -1;
            System.out.println("Backend A Status: " + status);

        } catch (Exception e) {

            consecutiveFailures++;
            consecutiveSuccesses = 0;

            if (consecutiveFailures >= 3) {
                status = BackendHealthStatus.UNHEALTHY;
            }    
                healthValue = (status == BackendHealthStatus.UNHEALTHY) ? 0 : -1;
            

            System.out.println("Backend A Status: " + status);
            System.out.println("Health check failed: " + e.getMessage());
        }
    }
}