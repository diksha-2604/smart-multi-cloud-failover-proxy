
package com.smcfp.gateway.loadbalancer;

import com.smcfp.gateway.model.Backend;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinStrategy implements LoadBalancingStrategy {

    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public Backend select(List<Backend> backends) {
        if (backends == null || backends.isEmpty()) {
            return null;
        }

        List<Backend> eligible = new ArrayList<>();

        for (Backend backend : backends) {
            if (backend != null && backend.isHealthy()) {
                eligible.add(backend);
            }
        }

        if (eligible.isEmpty()) {
            return null;
        }

        int i = index.getAndUpdate(x -> (x + 1) % eligible.size());
        return eligible.get(i);
    }
}
