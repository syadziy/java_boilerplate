package com.mac.boilerplate.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.TaskAuditResponse;
import com.mac.boilerplate.entities.dto.TaskDashboardResponse;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.boilerplate.entities.dto.TaskSearchResult;
import com.mac.boilerplate.service.TaskReportingService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class TaskInsightsControllerTest {

    @Test
    void delegatesReportingEndpoints() {
        TaskReportingService service = mock(TaskReportingService.class);
        TaskInsightsController controller = new TaskInsightsController(service);
        UUID id = UUID.randomUUID();
        TaskSearchResult searchResult = new TaskSearchResult(List.of(task(id)), 1, 20, 0);
        TaskDashboardResponse dashboard = new TaskDashboardResponse(
                Instant.EPOCH,
                Instant.EPOCH.plusSeconds(60),
                1,
                Map.of(TaskStatus.PENDING, 1L),
                0.0,
                List.of(task(id)));
        List<TaskAuditResponse> audit = List.of(new TaskAuditResponse(
                "audit-1", id, "TASK_CREATED", "A", null, TaskStatus.PENDING, Instant.EPOCH));

        when(service.search(any())).thenReturn(searchResult);
        when(service.searchIndex(eq("learn"), eq(TaskStatus.PENDING), eq(10))).thenReturn(searchResult);
        when(service.dashboard(any(), any())).thenReturn(dashboard);
        when(service.auditTrail(id)).thenReturn(audit);

        assertThat(controller.search("learn", TaskStatus.PENDING, Instant.EPOCH, Instant.EPOCH.plusSeconds(60), 20, 0)
                .getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.searchIndex("learn", TaskStatus.PENDING, 10).getBody().getData()).isEqualTo(searchResult);
        assertThat(controller.dashboard(Instant.EPOCH, Instant.EPOCH.plusSeconds(60)).getBody().getData())
                .isEqualTo(dashboard);
        assertThat(controller.auditTrail(id).getBody().getData()).isEqualTo(audit);
    }

    private static TaskResponse task(UUID id) {
        return new TaskResponse(id, "A", null, TaskStatus.PENDING, Instant.EPOCH, null);
    }
}
