package com.mac.boilerplate.controller;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.TaskAuditResponse;
import com.mac.boilerplate.entities.dto.TaskDashboardResponse;
import com.mac.boilerplate.entities.dto.TaskSearchCriteria;
import com.mac.boilerplate.entities.dto.TaskSearchResult;
import com.mac.boilerplate.service.TaskReportingService;
import com.mac.sdk_util.entities.dto.ResponseDTO;
import com.mac.sdk_util.helper.ResponseHelper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/tasks/insights")
public class TaskInsightsController {

    private final TaskReportingService reportingService;

    public TaskInsightsController(TaskReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseDTO<TaskSearchResult>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "20") @Min(1) @Positive int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset) {
        return ResponseHelper.httpOK(reportingService.search(
                new TaskSearchCriteria(query, status, from, to, limit, offset)));
    }

    @GetMapping("/search/index")
    public ResponseEntity<ResponseDTO<TaskSearchResult>> searchIndex(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "20") @Min(1) @Positive int limit) {
        return ResponseHelper.httpOK(reportingService.searchIndex(query, status, limit));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ResponseDTO<TaskDashboardResponse>> dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ResponseHelper.httpOK(reportingService.dashboard(from, to));
    }

    @GetMapping("/{taskId}/audit")
    public ResponseEntity<ResponseDTO<List<TaskAuditResponse>>> auditTrail(@PathVariable UUID taskId) {
        return ResponseHelper.httpOK(reportingService.auditTrail(taskId));
    }
}
