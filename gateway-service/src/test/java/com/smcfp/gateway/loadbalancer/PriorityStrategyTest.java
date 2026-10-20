package com.smcfp.gateway.loadbalancer;

import com.smcfp.gateway.model.Backend;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriorityStrategyTest {

    private final PriorityStrategy strategy = new PriorityStrategy();

    private Backend createBackend(String id, int priority, boolean healthy) {
        Backend backend = new Backend();
        backend.setId(id);
        backend.setPriority(priority);
        backend.setHealthy(healthy);
        return backend;
    }

    @Test
    void selectsBackendWithHighestPriority() {
        Backend b1 = createBackend("b1", 1, true);
        Backend b2 = createBackend("b2", 3, true);
        Backend b3 = createBackend("b3", 2, true);

        assertSame(b2, strategy.select(Arrays.asList(b1, b2, b3)));
    }

    @Test
    void ignoresUnhealthyBackendWithHighestPriority() {
        Backend b1 = createBackend("b1", 2, true);
        Backend b2 = createBackend("b2", 5, false);

        assertSame(b1, strategy.select(Arrays.asList(b1, b2)));
    }

    @Test
    void returnsNullForEmptyList() {
        assertNull(strategy.select(Collections.emptyList()));
    }

    @Test
    void returnsNullWhenAllBackendsAreUnhealthy() {
        Backend b1 = createBackend("b1", 1, false);
        Backend b2 = createBackend("b2", 3, false);

        assertNull(strategy.select(Arrays.asList(b1, b2)));
    }

    @Test
    void selectsFirstBackendWhenPrioritiesAreEqual() {
        Backend b1 = createBackend("b1", 3, true);
        Backend b2 = createBackend("b2", 3, true);

        assertSame(b1, strategy.select(Arrays.asList(b1, b2)));
    }

    @Test
    void returnsNullForNullList() {
        assertNull(strategy.select(null));
    }
}
