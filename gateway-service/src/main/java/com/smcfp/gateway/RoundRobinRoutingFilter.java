
package com.smcfp.gateway;

import com.smcfp.gateway.loadbalancer.RoundRobinStrategy;
import com.smcfp.gateway.model.Backend;
import com.smcfp.gateway.registry.BackendRegistry;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Component
public class RoundRobinRoutingFilter implements GlobalFilter, Ordered {

    private final BackendRegistry registry;
    private final RoundRobinStrategy strategy = new RoundRobinStrategy();

    public RoundRobinRoutingFilter(BackendRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String path = exchange.getRequest()
                .getURI()
                .getRawPath();

        if (!path.equals("/roundrobin")
                && !path.startsWith("/roundrobin/")) {
            return chain.filter(exchange);
        }

        List<Backend> backends = registry.getBackends();
        Backend selected = strategy.select(backends);

        if (selected == null) {
            exchange.getResponse()
                    .setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return exchange.getResponse().setComplete();
        }

        String backendPath = path.substring("/roundrobin".length());

        if (backendPath.isEmpty()) {
            backendPath = "/";
        }

        String query = exchange.getRequest()
                .getURI()
                .getRawQuery();

        String target = selected.getUrl() + backendPath;

        if (query != null && !query.isEmpty()) {
            target += "?" + query;
        }

        exchange.getAttributes().put(
                ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR,
                URI.create(target));

        System.out.println(
                "[ROUND ROBIN] Selected backend: "
                + selected.getProvider()
                + " | URL: " + target);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 10100;
    }
}
