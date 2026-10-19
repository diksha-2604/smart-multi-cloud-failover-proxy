package com.smcfp.gateway.registry;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "smcfp")
public class BackendRegistryProperties {

private List<Backend> backends = new ArrayList<>();

public List<Backend> getBackends() {
    return backends;
}

public void setBackends(List<Backend> backends) {
    this.backends = backends;
}

}