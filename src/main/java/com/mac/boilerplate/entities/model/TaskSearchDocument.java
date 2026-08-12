package com.mac.boilerplate.entities.model;

import com.mac.boilerplate.entities.constant.TaskStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "task-search")
public record TaskSearchDocument(
        @Id String id,
        UUID taskId,
        String title,
        String description,
        TaskStatus status,
        Instant createdAt,
        Instant completedAt) {}
