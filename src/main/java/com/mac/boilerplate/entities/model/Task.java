package com.mac.boilerplate.entities.model;

import com.mac.boilerplate.entities.constant.TaskStatus;
import java.time.Instant;
import java.util.UUID;

public record Task(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        Instant createdAt,
        Instant completedAt) {}
