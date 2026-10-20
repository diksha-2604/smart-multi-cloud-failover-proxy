
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

        // Exclude monitoring endpoints from request history.
        if (path.equals("/metrics/requests")
                || path.equals("/requests/history")) {
            return chain.filter(exchange);
        }

        String requestId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-SMCFP-Request-Id");

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        final String currentRequestId = requestId;

        ServerWebExchange updatedExchange = exchange.mutate()
                .request(builder -> builder.header(
                        "X-SMCFP-Request-Id",
                        currentRequestId))
                .build();

        // Identify direct backend routes immediately.
        String backend = identifyBackend(path);

        // For Round Robin, the backend is selected by a later filter.
        // Use a request-scoped mutable holder so that selection is visible
        // when the response completes.
        BackendHolder backendHolder = new BackendHolder(backend);

        updatedExchange.getAttributes().put(
                "smcfp.backendHolder", backendHolder);
        
        updatedExchange.getAttributes().put(
                "smcfp.requestId", currentRequestId);

        updatedExchange.getAttributes().put(
                "smcfp.requestPath", path);
        String attemptId = metrics.startAttempt(
                currentRequestId,
                1,
                path,
                backend);

        updatedExchange.getAttributes().put(
                "smcfp.attemptId", attemptId);

        if (!path.equals("/roundrobin")
                        && !path.startsWith("/roundrobin/")) {
                System.out.println(
                        "[REQUEST] ID: " + currentRequestId
                        + " | Path: " + path
                        + " | Backend: " + backend
                        + " | Attempt: 1");
        }

        return chain.filter(updatedExchange)
                .doOnError(error -> {
                    String selectedBackend = backendHolder.getBackend();

                    String reason = error.getMessage();
                    if (reason == null || reason.isBlank()) {
                        reason = error.getClass().getSimpleName();
                    }

                    metrics.updateBackend(attemptId, selectedBackend);
                    metrics.completeAttempt(attemptId, 502, reason);

                    System.out.println(
                            "[FAILED] Request ID: " + currentRequestId
                            + " | Backend: " + selectedBackend
                            + " | Status: 502"
                            + " | Reason: " + reason);
                })
                .onErrorResume(error -> {
                    if (!updatedExchange.getResponse().isCommitted()) {
                        updatedExchange.getResponse().setStatusCode(
                                HttpStatus.BAD_GATEWAY);
                    }
                    return updatedExchange.getResponse().setComplete();
                })
                .then(Mono.fromRunnable(() -> {
                    String selectedBackend = backendHolder.getBackend();

                    HttpStatusCode status =
                            updatedExchange.getResponse().getStatusCode();

                    int statusCode = status != null ? status.value() : 0;

                    metrics.updateBackend(attemptId, selectedBackend);
                    metrics.completeAttempt(attemptId, statusCode, null);

                    System.out.println(
                            "[RESPONSE] Request ID: " + currentRequestId
                            + " | Backend: " + selectedBackend
                            + " | Status: " + statusCode
                            + " | Result: "
                            + (statusCode >= 200 && statusCode < 300
                                    ? "PASSED" : "FAILED"));
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

    public static class BackendHolder {
        private volatile String backend;

        public BackendHolder(String backend) {
            this.backend = backend;
        }

        public String getBackend() {
            return backend;
        }

        public void setBackend(String backend) {
            if (backend != null && !backend.isBlank()) {
                this.backend = backend;
            }
        }
    }
}
