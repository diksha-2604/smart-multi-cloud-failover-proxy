package com.smcfp.gateway.registry;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backends")
public class BackendRegistryController {

private final BackendRegistryService registryService;

public BackendRegistryController(BackendRegistryService registryService) {
    this.registryService = registryService;
}

// Get all registered backends
@GetMapping
public Mono<List<Backend>> getAllBackends() {
    return Mono.just(registryService.getAllBackends());
}

// Get a backend by ID
@GetMapping("/{id}")
public Mono<ResponseEntity<Backend>> getBackendById(
        @PathVariable String id) {

    return Mono.just(
            registryService.getBackendById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build())
    );
}

// Register a new backend
@PostMapping
public Mono<ResponseEntity<?>> registerBackend(
        @RequestBody Backend backend) {

    try {
        Backend registered = registryService.registerBackend(backend);

        return Mono.just(
                ResponseEntity.status(HttpStatus.CREATED).body(registered)
        );

    } catch (IllegalArgumentException e) {
        return Mono.just(
                ResponseEntity.badRequest()
                        .body(Map.of("error", e.getMessage()))
        );
    }
}

// Update an existing backend
@PutMapping("/{id}")
public Mono<ResponseEntity<?>> updateBackend(
        @PathVariable String id,
        @RequestBody Backend backend) {

    try {
        Backend updated = registryService.updateBackend(id, backend);

        return Mono.just(ResponseEntity.ok(updated));

    } catch (IllegalArgumentException e) {
        HttpStatus status = e.getMessage().startsWith("Backend not found")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;

        return Mono.just(
                ResponseEntity.status(status)
                        .body(Map.of("error", e.getMessage()))
        );
    }
}

// Remove a backend
@DeleteMapping("/{id}")
public Mono<ResponseEntity<Void>> removeBackend(
        @PathVariable String id) {

    if (registryService.removeBackend(id)) {
        return Mono.just(ResponseEntity.noContent().build());
    }

    return Mono.just(ResponseEntity.notFound().build());
}

}