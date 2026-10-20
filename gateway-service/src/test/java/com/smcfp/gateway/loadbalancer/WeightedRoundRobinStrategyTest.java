package com.smcfp.gateway.loadbalancer;

import com.smcfp.gateway.model.Backend;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class WeightedRoundRobinStrategyTest {

    private final WeightedRoundRobinStrategy strategy =
            new WeightedRoundRobinStrategy();

    private Backend createBackend(String id, int weight, boolean healthy) {
        Backend backend = new Backend();
        backend.setId(id);
        backend.setWeight(weight);
        backend.setHealthy(healthy);
        return backend;
    }

    @Test
    void distributesSelectionsAccordingToWeights() {
        Backend b1 = createBackend("b1", 3, true);
        Backend b2 = createBackend("b2", 1, true);

        assertSame(b1, strategy.select(Arrays.asList(b1, b2)));
        assertSame(b1, strategy.select(Arrays.asList(b1, b2)));
        assertSame(b1, strategy.select(Arrays.asList(b1, b2)));
        assertSame(b2, strategy.select(Arrays.asList(b1, b2)));
    }

    @Test
    void skipsUnhealthyBackends() {
        Backend b1 = createBackend("b1", 3, false);
        Backend b2 = createBackend("b2", 1, true);

        assertSame(b2, strategy.select(Arrays.asList(b1, b2)));
    }

    @Test
    void skipsBackendsWithZeroOrNegativeWeights() {
        Backend b1 = createBackend("b1", 0, true);
        Backend b2 = createBackend("b2", -2, true);
        Backend b3 = createBackend("b3", 1, true);

        assertSame(b3, strategy.select(Arrays.asList(b1, b2, b3)));
    }

    @Test
    void returnsNullWhenNoBackendIsEligible() {
        Backend b1 = createBackend("b1", 0, true);
        Backend b2 = createBackend("b2", 2, false);

        assertNull(strategy.select(Arrays.asList(b1, b2)));
    }

    @Test
    void returnsNullForEmptyList() {
        assertNull(strategy.select(Collections.emptyList()));
    }

    @Test
    void returnsNullForNullList() {
        assertNull(strategy.select(null));
    }
}
