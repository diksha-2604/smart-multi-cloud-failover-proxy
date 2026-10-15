
package com.smcfp.gateway;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayRequestMetricsController {

    private final GatewayRequestMetrics metrics;

    public GatewayRequestMetricsController(
            GatewayRequestMetrics metrics) {
        this.metrics = metrics;
    }

    @GetMapping("/metrics/requests")
    public Map<String, Map<String, Long>> getRequestMetrics() {
        return metrics.getSummary();
    }
}
