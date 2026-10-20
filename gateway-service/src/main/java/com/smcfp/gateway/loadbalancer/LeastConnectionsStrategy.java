package com.smcfp.gateway.loadbalancer;

import com.smcfp.gateway.model.Backend;
import java.util.List;

public class LeastConnectionsStrategy implements LoadBalancingStrategy {

    @Override
    public Backend select(List<Backend> backends) {
        if (backends == null || backends.isEmpty()) {
            return null;
        }

        Backend selected = null;

        for (Backend backend : backends) {
            if (backend == null || !backend.isHealthy()) {
                continue;
            }

            if (selected == null ||
                    backend.getActiveConnections()
                            < selected.getActiveConnections()) {
                selected = backend;
            }
        }

        return selected;
    }
}
