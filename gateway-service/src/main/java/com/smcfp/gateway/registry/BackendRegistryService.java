package com.smcfp.gateway.registry;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BackendRegistryService {

private final Map<String, Backend> backends = new LinkedHashMap<>();

public BackendRegistryService(BackendRegistryProperties properties) {
    for (Backend backend : properties.getBackends()) {
        validateBackend(backend);

        if (backends.containsKey(backend.getId())) {
            throw new IllegalStateException(
                    "Duplicate backend ID: " + backend.getId()
            );
        }

        backends.put(backend.getId(), backend);
    }
}

// Retrieve all registered backends
public List<Backend> getAllBackends() {
    return new ArrayList<>(backends.values());
}

// Retrieve a backend by its ID
public Optional<Backend> getBackendById(String id) {
    return Optional.ofNullable(backends.get(id));
}

// Register a new backend
public Backend registerBackend(Backend backend) {
    validateBackend(backend);

    if (backends.containsKey(backend.getId())) {
        throw new IllegalArgumentException(
                "Backend already registered: " + backend.getId()
        );
    }

    backends.put(backend.getId(), backend);
    return backend;
}

// Update an existing backend
public Backend updateBackend(String id, Backend updatedBackend) {
    if (!backends.containsKey(id)) {
        throw new IllegalArgumentException(
                "Backend not found: " + id
        );
    }

    validateBackend(updatedBackend);
    updatedBackend.setId(id);

    backends.put(id, updatedBackend);
    return updatedBackend;
}

// Remove a backend
public boolean removeBackend(String id) {
    return backends.remove(id) != null;
}

// Basic validation
private void validateBackend(Backend backend) {
    if (backend == null) {
        throw new IllegalArgumentException(
                "Backend cannot be null"
        );
    }

    if (backend.getId() == null || backend.getId().isBlank()) {
        throw new IllegalArgumentException(
                "Backend ID cannot be empty"
        );
    }

    if (backend.getProvider() == null
            || backend.getProvider().isBlank()) {
        throw new IllegalArgumentException(
                "Backend provider cannot be empty"
        );
    }

    if (backend.getUrl() == null || backend.getUrl().isBlank()) {
        throw new IllegalArgumentException(
                "Backend URL cannot be empty"
        );
    }

    if (backend.getPriority() < 1) {
        throw new IllegalArgumentException(
                "Priority must be at least 1"
        );
    }

    if (backend.getWeight() < 1) {
        throw new IllegalArgumentException(
                "Weight must be at least 1"
        );
    }
}

}