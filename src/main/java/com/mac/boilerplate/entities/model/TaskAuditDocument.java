package com.mac.boilerplate.entities.model;

import com.mac.boilerplate.entities.constant.TaskStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("task_audit")
public record TaskAuditDocument(
        @Id String id,
        UUID taskId,
        String eventType,
        String title,
        String description,
        TaskStatus status,
        Instant occurredAt) {}
