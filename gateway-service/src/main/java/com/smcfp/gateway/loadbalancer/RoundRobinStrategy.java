package com.smcfp.gateway.loadbalancer;

import com.smcfp.gateway.model.Backend;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinStrategy implements LoadBalancingStrategy{
    private final AtomicInteger index = new AtomicInteger(0);

    public Backend select(List<Backend> backends) {
        if (backends == null || backends.isEmpty()) {
            return null;
        }
        int i = index.getAndUpdate(x -> (x + 1) % backends.size());
        Backend selected = backends.get(i);

        return selected;
    }
}