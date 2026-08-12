package com.mac.boilerplate.entities.dto;

import com.mac.boilerplate.entities.constant.TaskStatus;
import java.time.Instant;
import java.util.UUID;

public record TaskAuditResponse(
        String id,
        UUID taskId,
        String eventType,
        String title,
        String description,
        TaskStatus status,
        Instant occurredAt) {}
