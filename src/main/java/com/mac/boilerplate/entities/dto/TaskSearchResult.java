package com.mac.boilerplate.entities.dto;

import java.util.List;

public record TaskSearchResult(
        List<TaskResponse> items,
        long totalCount,
        int limit,
        int offset) {}
