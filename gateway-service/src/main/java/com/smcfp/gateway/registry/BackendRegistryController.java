package com.smcfp.gateway.registry;

import com.smcfp.gateway.model.Backend;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backends")
public class BackendRegistryController {

    private final BackendRegistry registry;

    public BackendRegistryController(BackendRegistry registry) {
        this.registry = registry;
    }

    @GetMapping
    public Mono<List<Backend>> getAllBackends() {
        return Mono.just(registry.getBackends());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Backend>> getBackendById(
            @PathVariable String id) {
        return Mono.just(ResponseEntity.ofNullable(
                registry.getBackendById(id)));
    }

    @PostMapping
    public Mono<ResponseEntity<?>> registerBackend(
            @RequestBody Backend backend) {
        try {
            Backend registered = registry.registerBackend(backend);
            return Mono.just(ResponseEntity.status(HttpStatus.CREATED)
                    .body(registered));
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage())));
        }
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<?>> updateBackend(
            @PathVariable String id,
            @RequestBody Backend backend) {
        try {
            return Mono.just(ResponseEntity.ok(
                    registry.updateBackend(id, backend)));
        } catch (IllegalArgumentException e) {
            HttpStatus status = e.getMessage().startsWith("Backend not found")
                    ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return Mono.just(ResponseEntity.status(status)
                    .body(Map.of("error", e.getMessage())));
        }
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> removeBackend(
            @PathVariable String id) {
        if (registry.removeBackend(id)) {
            return Mono.just(ResponseEntity.noContent().build());
        }
        return Mono.just(ResponseEntity.notFound().build());
    }
}
