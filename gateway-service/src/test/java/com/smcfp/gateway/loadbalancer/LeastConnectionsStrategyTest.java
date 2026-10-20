package com.smcfp.gateway.loadbalancer;

import com.smcfp.gateway.model.Backend;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class LeastConnectionsStrategyTest {

    private final LeastConnectionsStrategy strategy =
            new LeastConnectionsStrategy();

    private Backend createBackend(String id, int connections,
                                  boolean healthy) {
        Backend backend = new Backend();
        backend.setId(id);
        backend.setActiveConnections(connections);
        backend.setHealthy(healthy);
        return backend;
    }

    @Test
    void selectsBackendWithFewestConnections() {
        Backend b1 = createBackend("b1", 8, true);
        Backend b2 = createBackend("b2", 3, true);
        Backend b3 = createBackend("b3", 5, true);

        assertSame(b2, strategy.select(Arrays.asList(b1, b2, b3)));
    }

    @Test
    void ignoresUnhealthyBackendWithFewestConnections() {
        Backend b1 = createBackend("b1", 2, false);
        Backend b2 = createBackend("b2", 5, true);

        assertSame(b2, strategy.select(Arrays.asList(b1, b2)));
    }

    @Test
    void selectsFirstBackendWhenConnectionsAreEqual() {
        Backend b1 = createBackend("b1", 3, true);
        Backend b2 = createBackend("b2", 3, true);

        assertSame(b1, strategy.select(Arrays.asList(b1, b2)));
    }

    @Test
    void returnsNullWhenAllBackendsAreUnhealthy() {
        Backend b1 = createBackend("b1", 1, false);
        Backend b2 = createBackend("b2", 3, false);

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
