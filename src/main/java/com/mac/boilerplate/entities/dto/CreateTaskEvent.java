package com.mac.boilerplate.entities.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskEvent(
        @NotBlank(message = "Event ID is required") String eventId,
        @NotBlank(message = "Title is required")
        @Size(max = 120, message = "Title must not exceed 120 characters") String title,
        @Size(max = 1000, message = "Description must not exceed 1000 characters") String description,
        String traceId) {}
