package com.smcfp.gateway.loadbalancer;

import java.util.Locale;

public class LoadBalancingStrategyFactory {

    public static LoadBalancingStrategy getStrategy(String strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Load balancing strategy cannot be null");
        }

        switch (strategy.trim().toUpperCase(Locale.ROOT)) {
            case "ROUND_ROBIN":
                return new RoundRobinStrategy();

            case "PRIORITY":
                return new PriorityStrategy();

            case "WEIGHTED_ROUND_ROBIN":
                return new WeightedRoundRobinStrategy();

            case "LEAST_CONNECTIONS":
                return new LeastConnectionsStrategy();

            default:
                throw new IllegalArgumentException(
                        "Unknown load balancing strategy: " + strategy);
        }
    }
}
