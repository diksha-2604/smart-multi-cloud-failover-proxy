
package com.smcfp.gateway.registry;

import com.smcfp.gateway.model.Backend;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class BackendRegistry {

    private final List<Backend> backends = new CopyOnWriteArrayList<>();

    public BackendRegistry() {
        backends.add(createBackend(
                "aws", "AWS", "http://localhost:8081", 3, 1));

        backends.add(createBackend(
                "azure", "Azure", "http://localhost:8082", 2, 1));

        backends.add(createBackend(
                "gcp", "GCP", "http://localhost:8083", 1, 1));
    }

    private Backend createBackend(
            String id, String provider, String url,
            int priority, int weight) {

        Backend backend = new Backend();
        backend.setId(id);
        backend.setProvider(provider);
        backend.setUrl(url);
        backend.setPriority(priority);
        backend.setWeight(weight);
        backend.setActiveConnections(0);
        backend.setHealthy(true);

        return backend;
    }

    public List<Backend> getBackends() {
        return List.copyOf(backends);
    }
}
