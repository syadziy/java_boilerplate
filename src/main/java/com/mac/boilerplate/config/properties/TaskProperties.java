package com.mac.boilerplate.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("task")
public record TaskProperties(Cache cache, Cleanup cleanup) {

    public TaskProperties(
            @DefaultValue Cache cache,
            @DefaultValue Cleanup cleanup) {
        this.cache = cache;
        this.cleanup = cleanup;
    }

    public record Cache(@DefaultValue("10m") Duration ttl) {}

    public record Cleanup(@DefaultValue("30d") Duration retention) {}
}
