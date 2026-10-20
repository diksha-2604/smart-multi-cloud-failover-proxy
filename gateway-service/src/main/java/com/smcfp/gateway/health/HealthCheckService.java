package com.smcfp.gateway.health;

import com.smcfp.gateway.model.Backend;
import com.smcfp.gateway.registry.BackendRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HealthCheckService {

    private static final Logger log =
            LoggerFactory.getLogger(HealthCheckService.class);

    private final BackendRegistry backendRegistry;
    private final HttpClient httpClient;

    @Value("${smcfp.health-check.path:/health}")
    private String healthPath;

    public HealthCheckService(BackendRegistry backendRegistry) {
        this.backendRegistry = backendRegistry;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @Scheduled(
            fixedDelayString = "${smcfp.health-check.interval-ms:5000}",
            initialDelayString = "${smcfp.health-check.initial-delay-ms:1000}"
    )
    public void checkBackends() {
        for (Backend backend : backendRegistry.getBackends()) {
            boolean wasHealthy = backend.isHealthy();
            boolean isHealthy = checkBackend(backend);

            backend.setHealthy(isHealthy);

            if (wasHealthy != isHealthy) {
                if (isHealthy) {
                    log.info("Backend {} ({}) recovered",
                            backend.getId(), backend.getProvider());
                } else {
                    log.warn("Backend {} ({}) is unhealthy",
                            backend.getId(), backend.getProvider());
                }
            }
        }
    }

    private boolean checkBackend(Backend backend) {
        try {
            String baseUrl = backend.getUrl().replaceAll("/+$", "");
            String path = healthPath.startsWith("/")
                    ? healthPath
                    : "/" + healthPath;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<Void> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.discarding());

            return response.statusCode() >= 200
                    && response.statusCode() < 300;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.debug("Health check failed for {}: {}",
                    backend.getId(), e.getMessage());
            return false;
        }
    }
}
