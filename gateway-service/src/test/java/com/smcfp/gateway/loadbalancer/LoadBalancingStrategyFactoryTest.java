package com.smcfp.gateway.loadbalancer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoadBalancingStrategyFactoryTest {

    @Test
    void createsRoundRobinStrategy() {
        assertInstanceOf(
                RoundRobinStrategy.class,
                LoadBalancingStrategyFactory.getStrategy("ROUND_ROBIN"));
    }

    @Test
    void createsPriorityStrategy() {
        assertInstanceOf(
                PriorityStrategy.class,
                LoadBalancingStrategyFactory.getStrategy("PRIORITY"));
    }

    @Test
    void createsWeightedRoundRobinStrategy() {
        assertInstanceOf(
                WeightedRoundRobinStrategy.class,
                LoadBalancingStrategyFactory.getStrategy(
                        "WEIGHTED_ROUND_ROBIN"));
    }

    @Test
    void createsLeastConnectionsStrategy() {
        assertInstanceOf(
                LeastConnectionsStrategy.class,
                LoadBalancingStrategyFactory.getStrategy(
                        "LEAST_CONNECTIONS"));
    }

    @Test
    void acceptsLowercaseStrategyName() {
        assertInstanceOf(
                RoundRobinStrategy.class,
                LoadBalancingStrategyFactory.getStrategy("round_robin"));
    }

    @Test
    void rejectsUnknownStrategy() {
        assertThrows(
                IllegalArgumentException.class,
                () -> LoadBalancingStrategyFactory.getStrategy("UNKNOWN"));
    }

    @Test
    void rejectsNullStrategy() {
        assertThrows(
                IllegalArgumentException.class,
                () -> LoadBalancingStrategyFactory.getStrategy(null));
    }
}
