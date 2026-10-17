
package com.smcfp.gateway;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

@Service
public class GatewayRequestMetrics {

    private final List<RequestAttempt> history =
            new CopyOnWriteArrayList<>();

    private final Map<String, RequestAttempt> attemptsById =
            new ConcurrentHashMap<>();

    private static final int MAX_HISTORY = 1000;

    // Stored outside src/main/resources.
    private final Path historyFile =
            Path.of("data", "request-history.yaml");

    private final ObjectMapper yamlMapper =
            new ObjectMapper(new YAMLFactory());

    public GatewayRequestMetrics() {
        loadHistory();
    }

    public synchronized String startAttempt(
            String requestId,
            int attemptNumber,
            String path,
            String backend) {

        String attemptId = UUID.randomUUID().toString();

        RequestAttempt attempt = new RequestAttempt(
                attemptId,
                requestId,
                OffsetDateTime.now().toString(),
                path,
                backend,
                attemptNumber
        );

        attemptsById.put(attemptId, attempt);
        history.add(attempt);

        trimHistory();
        saveHistory();

        return attemptId;
    }

    public synchronized void completeAttempt(
            String attemptId,
            int statusCode,
            String exceptionReason) {

        RequestAttempt attempt = attemptsById.get(attemptId);

        if (attempt == null) {
            return;
        }

        // Do not count the same attempt more than once.
        if (!"PROCESSING".equals(attempt.getResult())) {
            return;
        }

        attempt.setCompletedAt(OffsetDateTime.now().toString());
        attempt.setStatusCode(statusCode);

        if (statusCode >= 200 && statusCode < 300) {
            attempt.setResult("PASSED");
            attempt.setFailureReason(null);
        } else {
            attempt.setResult("FAILED");

            String reason = exceptionReason;

            if (reason == null || reason.isBlank()) {
                reason = reasonForStatus(statusCode);
            }

            attempt.setFailureReason(reason);
        }

        saveHistory();
    }

    private String reasonForStatus(int statusCode) {
        return switch (statusCode) {
            case 0 -> "No HTTP response status was available";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            default -> "HTTP error " + statusCode;
        };
    }

    public synchronized List<RequestAttempt> getHistory() {
        List<RequestAttempt> result = new ArrayList<>(history);

        result.sort(
                Comparator.comparing(
                        RequestAttempt::getTimestamp
                ).reversed()
        );

        return result;
    }
    public synchronized Map<String, Map<String, Long>> getSummary() {
        Map<String, Map<String, Long>> summary = new TreeMap<>();

        for (RequestAttempt attempt : history) {
            String backend = attempt.getBackend();

            if (backend == null || backend.isBlank()) {
                backend = "UNKNOWN";
            }

            Map<String, Long> values = summary.computeIfAbsent(
                    backend,
                    key -> {
                        Map<String, Long> counts = new LinkedHashMap<>();
                        counts.put("received", 0L);
                        counts.put("passed", 0L);
                        counts.put("failed", 0L);
                        return counts;
                    }
            );

            values.put("received", values.get("received") + 1);

            if ("PASSED".equals(attempt.getResult())) {
                values.put("passed", values.get("passed") + 1);
            } else if ("FAILED".equals(attempt.getResult())) {
                values.put("failed", values.get("failed") + 1);
            }
        }

        return summary;
    }


    public void printSummary() {
        System.out.println();
        System.out.println("========== REQUEST SUMMARY ==========");

        Map<String, Map<String, Long>> summary = getSummary();

        summary.forEach((backend, counts) -> {
            System.out.printf(
                    "%-6s | Received: %d | Passed: %d | Failed: %d%n",
                    backend,
                    counts.get("received"),
                    counts.get("passed"),
                    counts.get("failed")
            );
        });

        System.out.println("=====================================");
        System.out.println();
    }
    private void trimHistory() {
        while (history.size() > MAX_HISTORY) {
            RequestAttempt oldestCompleted = null;

            for (RequestAttempt attempt : history) {
                if (!"PROCESSING".equals(attempt.getResult())) {
                    oldestCompleted = attempt;
                    break;
                }
            }

            // Keep unfinished requests so they can still be completed.
            if (oldestCompleted == null) {
                return;
            }

            history.remove(oldestCompleted);
            attemptsById.remove(oldestCompleted.getAttemptId());
        }
    }
    private synchronized void loadHistory() {
        if (!Files.exists(historyFile)) {
            System.out.println(
                    "[HISTORY] No saved history found. Starting fresh."
            );
            return;
        }

        try {
            List<RequestAttempt> saved = yamlMapper.readValue(
                    historyFile.toFile(),
                    new TypeReference<List<RequestAttempt>>() {}
            );

            if (saved == null) {
                return;
            }

            for (RequestAttempt attempt : saved) {
                if (attempt == null || attempt.getAttemptId() == null) {
                    continue;
                }

                // An unfinished request cannot continue after a restart.
                if ("PROCESSING".equals(attempt.getResult())) {
                    attempt.setResult("FAILED");
                    attempt.setStatusCode(0);
                    attempt.setCompletedAt(
                            OffsetDateTime.now().toString()
                    );
                    attempt.setFailureReason(
                            "Gateway restarted before the request completed"
                    );
                }

                history.add(attempt);
                attemptsById.put(attempt.getAttemptId(), attempt);

            }

            trimHistory();

            System.out.println(
                    "[HISTORY] Loaded " + history.size()
                    + " saved request records."
            );

            // Save any unfinished records marked as failed during recovery.
            saveHistory();

        } catch (IOException e) {
            System.err.println(
                    "[HISTORY ERROR] Could not load saved history: "
                    + e.getMessage()
            );
        }
    }

    private synchronized void saveHistory() {
        Path temporaryFile = null;

        try {
            Files.createDirectories(historyFile.getParent());

            temporaryFile = Files.createTempFile(
                    historyFile.getParent(),
                    "request-history-",
                    ".tmp"
            );

            yamlMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(temporaryFile.toFile(), history);

            try {
                Files.move(
                        temporaryFile,
                        historyFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(
                        temporaryFile,
                        historyFile,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (IOException e) {
            System.err.println(
                    "[HISTORY ERROR] Could not save request history: "
                    + e.getMessage()
            );
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ignored) {
                    // A leftover temporary file does not stop the Gateway.
                }
            }
        }
    }
}
