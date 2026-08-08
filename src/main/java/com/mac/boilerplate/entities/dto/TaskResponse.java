package com.mac.boilerplate.entities.dto;

import com.mac.boilerplate.entities.constant.TaskStatus;
import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        Instant createdAt,
        Instant completedAt) {}
