package com.smcfp.gateway.registry;

public class Backend {

private String id;
private String provider;
private String url;
private int priority;
private int weight;
private boolean enabled;

public Backend() {
}

public Backend(String id, String provider, String url,
               int priority, int weight, boolean enabled) {
    this.id = id;
    this.provider = provider;
    this.url = url;
    this.priority = priority;
    this.weight = weight;
    this.enabled = enabled;
}

public String getId() {
    return id;
}

public void setId(String id) {
    this.id = id;
}

public String getProvider() {
    return provider;
}

public void setProvider(String provider) {
    this.provider = provider;
}

public String getUrl() {
    return url;
}

public void setUrl(String url) {
    this.url = url;
}

public int getPriority() {
    return priority;
}

public void setPriority(int priority) {
    this.priority = priority;
}

public int getWeight() {
    return weight;
}

public void setWeight(int weight) {
    this.weight = weight;
}

public boolean isEnabled() {
    return enabled;
}

public void setEnabled(boolean enabled) {
    this.enabled = enabled;
}

}