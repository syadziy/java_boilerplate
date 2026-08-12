package com.mac.boilerplate.service;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.TaskAuditResponse;
import com.mac.boilerplate.entities.dto.TaskDashboardResponse;
import com.mac.boilerplate.entities.dto.TaskSearchCriteria;
import com.mac.boilerplate.entities.dto.TaskSearchResult;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TaskReportingService {

    TaskSearchResult search(TaskSearchCriteria criteria);

    TaskSearchResult searchIndex(String query, TaskStatus status, int limit);

    TaskDashboardResponse dashboard(Instant from, Instant to);

    List<TaskAuditResponse> auditTrail(UUID taskId);
}
