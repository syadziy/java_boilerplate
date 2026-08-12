package com.mac.boilerplate.entities.mapper;

import com.mac.boilerplate.entities.dto.TaskAuditResponse;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.boilerplate.entities.model.TaskAuditDocument;
import com.mac.boilerplate.entities.model.TaskSearchDocument;
import com.mac.boilerplate.entities.model.TaskSearchRow;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TaskProjectionMapper {

    public TaskSearchDocument toSearchDocument(TaskResponse response) {
        return new TaskSearchDocument(
                response.id().toString(),
                response.id(),
                response.title(),
                response.description(),
                response.status(),
                response.createdAt(),
                response.completedAt());
    }

    public TaskAuditDocument toAuditDocument(TaskResponse response, String eventType) {
        return new TaskAuditDocument(
                UUID.randomUUID().toString(),
                response.id(),
                eventType,
                response.title(),
                response.description(),
                response.status(),
                response.completedAt() == null ? response.createdAt() : response.completedAt());
    }

    public TaskResponse toResponse(TaskSearchRow row) {
        return new TaskResponse(
                row.id(),
                row.title(),
                row.description(),
                row.status(),
                row.createdAt(),
                row.completedAt());
    }

    public TaskAuditResponse toResponse(TaskAuditDocument document) {
        return new TaskAuditResponse(
                document.id(),
                document.taskId(),
                document.eventType(),
                document.title(),
                document.description(),
                document.status(),
                document.occurredAt());
    }
}
