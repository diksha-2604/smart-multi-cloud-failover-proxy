package com.smcfp.gateway.loadbalancer;

import com.smcfp.gateway.model.Backend;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class RoundRobinStrategyTest {

    @Test
    void shouldSelectBackendsInRoundRobinOrder() {
        Backend b1 = new Backend();
        Backend b2 = new Backend();
        Backend b3 = new Backend();

        List<Backend> backends = List.of(b1, b2, b3);
        RoundRobinStrategy strategy = new RoundRobinStrategy();

        assertSame(b1, strategy.select(backends));
        assertSame(b2, strategy.select(backends));
        assertSame(b3, strategy.select(backends));
        assertSame(b1, strategy.select(backends));
    }

    @Test
    void shouldReturnNullForEmptyBackendList() {
        RoundRobinStrategy strategy = new RoundRobinStrategy();
        assertNull(strategy.select(List.of()));
    }

    @Test
    void shouldAlwaysSelectSingleBackend() {
        Backend b1 = new Backend();
        RoundRobinStrategy strategy = new RoundRobinStrategy();
        List<Backend> backends = List.of(b1);

        assertSame(b1, strategy.select(backends));
        assertSame(b1, strategy.select(backends));
        assertSame(b1, strategy.select(backends));
    }

    @Test
    void shouldHandleConcurrentRequests() throws Exception {
        Backend b1 = new Backend();
        Backend b2 = new Backend();
        Backend b3 = new Backend();

        List<Backend> backends = List.of(b1, b2, b3);
        RoundRobinStrategy strategy = new RoundRobinStrategy();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        try {
            List<Callable<Backend>> tasks = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                tasks.add(() -> strategy.select(backends));
            }

            List<Future<Backend>> results = executor.invokeAll(tasks);
            int c1 = 0, c2 = 0, c3 = 0;

            for (Future<Backend> result : results) {
                Backend selected = result.get();

                if (selected == b1) {
                    c1++;
                } else if (selected == b2) {
                    c2++;
                } else if (selected == b3) {
                    c3++;
                }
            }

            assertEquals(34, c1);
            assertEquals(33, c2);
            assertEquals(33, c3);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void shouldReturnNullForNullBackendList() {
        RoundRobinStrategy strategy = new RoundRobinStrategy();
        assertNull(strategy.select(null));
    }
}
