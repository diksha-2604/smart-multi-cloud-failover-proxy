
package com.smcfp.gateway;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestHistoryController {

    private final GatewayRequestMetrics metrics;

    public RequestHistoryController(GatewayRequestMetrics metrics) {
        this.metrics = metrics;
    }

    @GetMapping("/requests/history")
    public List<RequestAttempt> getRequestHistory() {
        return metrics.getHistory();
    }
}
