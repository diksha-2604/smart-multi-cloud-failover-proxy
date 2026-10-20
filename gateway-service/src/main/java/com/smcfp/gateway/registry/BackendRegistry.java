package com.smcfp.gateway.registry;

import com.smcfp.gateway.model.Backend;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@ConfigurationProperties(prefix = "smcfp")
public class BackendRegistry {

    private final ConcurrentMap<String, Backend> backends =
            new ConcurrentHashMap<>();

    public void setBackends(List<Backend> configuredBackends) {
        if (configuredBackends == null) {
            return;
        }

        ConcurrentMap<String, Backend> loaded = new ConcurrentHashMap<>();

        for (Backend backend : configuredBackends) {
            validateBackend(backend);

            if (loaded.putIfAbsent(backend.getId(), backend) != null) {
                throw new IllegalStateException(
                        "Duplicate backend ID: " + backend.getId());
            }

            backend.setActiveConnections(0);
            backend.setHealthy(true);
        }

        backends.clear();
        backends.putAll(loaded);
    }

    public List<Backend> getBackends() {
        List<Backend> result = new ArrayList<>(backends.values());
        result.sort(Comparator.comparingInt(Backend::getPriority)
                .thenComparing(Backend::getId));
        return result;
    }

    public Backend getBackendById(String id) {
        return backends.get(id);
    }

    public Backend registerBackend(Backend backend) {
        validateBackend(backend);

        if (backends.putIfAbsent(backend.getId(), backend) != null) {
            throw new IllegalArgumentException(
                    "Backend already registered: " + backend.getId());
        }

        return backend;
    }

    public Backend updateBackend(String id, Backend updatedBackend) {
        validateBackend(updatedBackend);

        if (!backends.containsKey(id)) {
            throw new IllegalArgumentException("Backend not found: " + id);
        }

        updatedBackend.setId(id);
        backends.put(id, updatedBackend);
        return updatedBackend;
    }

    public boolean removeBackend(String id) {
        return backends.remove(id) != null;
    }

    private void validateBackend(Backend backend) {
        if (backend == null) {
            throw new IllegalArgumentException("Backend cannot be null");
        }
        if (backend.getId() == null || backend.getId().isBlank()) {
            throw new IllegalArgumentException("Backend ID cannot be empty");
        }
        if (backend.getProvider() == null || backend.getProvider().isBlank()) {
            throw new IllegalArgumentException("Backend provider cannot be empty");
        }
        if (backend.getUrl() == null || backend.getUrl().isBlank()) {
            throw new IllegalArgumentException("Backend URL cannot be empty");
        }
        if (backend.getPriority() < 1) {
            throw new IllegalArgumentException("Priority must be at least 1");
        }
        if (backend.getWeight() < 1) {
            throw new IllegalArgumentException("Weight must be at least 1");
        }
    }
}
