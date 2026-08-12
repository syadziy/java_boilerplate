package com.mac.boilerplate.entities.dto;

import com.mac.boilerplate.entities.constant.TaskStatus;
import java.time.Instant;
import java.util.UUID;

public record TaskSearchCriteria(
        String query,
        TaskStatus status,
        Instant from,
        Instant to,
        int limit,
        int offset) {

    public String cacheKey() {
        return String.join("|",
                normalize(query),
                status == null ? "ALL" : status.name(),
                from == null ? "NULL" : from.toString(),
                to == null ? "NULL" : to.toString(),
                Integer.toString(limit),
                Integer.toString(offset));
    }

    public String indexCacheKey() {
        return String.join("|",
                normalize(query),
                status == null ? "ALL" : status.name(),
                Integer.toString(limit));
    }

    public String dashboardCacheKey() {
        return String.join("|",
                from == null ? "NULL" : from.toString(),
                to == null ? "NULL" : to.toString());
    }

    public UUID cursor() {
        return UUID.nameUUIDFromBytes(cacheKey().getBytes());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
