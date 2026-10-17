package com.smcfp.gateway;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class GatewayLoggingFilter implements GlobalFilter, Ordered {

    private final GatewayRequestMetrics metrics;

    public GatewayLoggingFilter(GatewayRequestMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Do not record calls made to our own monitoring endpoints.
        if (path.equals("/metrics/requests")
                || path.equals("/requests/history")) {
            return chain.filter(exchange);
        }

        String backend = identifyBackend(path);

        String requestId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-SMCFP-Request-Id");

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        final String currentRequestId = requestId;

        // Pass the request ID downstream.
        ServerWebExchange updatedExchange = exchange.mutate()
                .request(builder -> builder.header(
                        "X-SMCFP-Request-Id",
                        currentRequestId))
                .build();

        String attemptId = metrics.startAttempt(
                currentRequestId,
                1,
                path,
                backend
        );

        System.out.println(
                "[REQUEST] ID: " + currentRequestId
                + " | Path: " + path
                + " | Backend: " + backend
                + " | Attempt: 1"
        );

        return chain.filter(updatedExchange)
                .doOnError(error -> {
                    String reason = error.getMessage();

                    if (reason == null || reason.isBlank()) {
                        reason = error.getClass().getSimpleName();
                    }

                    // Record the failed backend connection as 502.
                    metrics.completeAttempt(
                            attemptId, 502, reason);

                    System.out.println(
                            "[FAILED] Request ID: " + currentRequestId
                            + " | Backend: " + backend
                            + " | Status: 502"
                            + " | Reason: " + reason
                    );
                })
                .onErrorResume(error -> {
                    // Return a gateway error if the response is not committed.
                    if (!updatedExchange.getResponse().isCommitted()) {
                        updatedExchange.getResponse().setStatusCode(
                                HttpStatus.BAD_GATEWAY);
                    }

                    return updatedExchange.getResponse().setComplete();
                })
                .then(Mono.fromRunnable(() -> {
                    HttpStatusCode status =
                            updatedExchange.getResponse().getStatusCode();

                    int statusCode = status != null
                            ? status.value() : 0;

                    metrics.completeAttempt(
                            attemptId, statusCode, null);

                    System.out.println(
                            "[RESPONSE] Request ID: " + currentRequestId
                            + " | Backend: " + backend
                            + " | Status: " + statusCode
                            + " | Result: "
                            + (statusCode >= 200 && statusCode < 300
                                    ? "PASSED" : "FAILED")
                    );
                }));
    }

    private String identifyBackend(String path) {
        if (path.equals("/backend1")
                || path.startsWith("/backend1/")) {
            return "AWS";
        }

        if (path.equals("/backend2")
                || path.startsWith("/backend2/")) {
            return "Azure";
        }

        if (path.equals("/backend3")
                || path.startsWith("/backend3/")) {
            return "GCP";
        }

        return "UNKNOWN";
    }

    @Override
    public int getOrder() {
        return -1;
    }
}