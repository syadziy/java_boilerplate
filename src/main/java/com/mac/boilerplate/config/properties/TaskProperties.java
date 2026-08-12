package com.mac.boilerplate.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("task")
public record TaskProperties(Cache cache, Cleanup cleanup, Reporting reporting) {

    public TaskProperties(
            @DefaultValue Cache cache,
            @DefaultValue Cleanup cleanup,
            @DefaultValue Reporting reporting) {
        this.cache = cache;
        this.cleanup = cleanup;
        this.reporting = reporting;
    }

    public record Cache(@DefaultValue("10m") Duration ttl) {}

    public record Cleanup(@DefaultValue("30d") Duration retention) {}

    public record Reporting(
            @DefaultValue("5m") Duration cacheTtl,
            @DefaultValue("P30D") Duration defaultWindow,
            @DefaultValue("task-search") String elasticIndex,
            @DefaultValue("task_audit") String auditCollection) {}
}
