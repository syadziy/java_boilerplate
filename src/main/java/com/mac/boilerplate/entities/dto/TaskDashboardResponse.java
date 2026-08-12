package com.mac.boilerplate.entities.dto;

import com.mac.boilerplate.entities.constant.TaskStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record TaskDashboardResponse(
        Instant from,
        Instant to,
        long totalCount,
        Map<TaskStatus, Long> countsByStatus,
        double averageCompletionMinutes,
        List<TaskResponse> recentTasks) {}
