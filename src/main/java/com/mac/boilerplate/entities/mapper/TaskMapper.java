package com.mac.boilerplate.entities.mapper;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.CreateTaskRequest;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.boilerplate.entities.model.Task;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public Task toModel(CreateTaskRequest request, UUID id, Instant now) {
        return new Task(
                id,
                request.title().trim(),
                normalizeNullable(request.description()),
                TaskStatus.PENDING,
                now,
                null);
    }

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.id(), task.title(), task.description(), task.status(), task.createdAt(), task.completedAt());
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
