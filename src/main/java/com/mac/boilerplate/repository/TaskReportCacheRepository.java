package com.mac.boilerplate.repository;

import com.mac.boilerplate.entities.dto.TaskDashboardResponse;
import com.mac.boilerplate.entities.dto.TaskSearchCriteria;
import com.mac.boilerplate.entities.dto.TaskSearchResult;
import java.time.Instant;
import java.util.Optional;

public interface TaskReportCacheRepository {

    Optional<TaskSearchResult> findSearch(TaskSearchCriteria criteria);

    void putSearch(TaskSearchCriteria criteria, TaskSearchResult result);

    Optional<TaskDashboardResponse> findDashboard(Instant from, Instant to);

    void putDashboard(Instant from, Instant to, TaskDashboardResponse result);
}
