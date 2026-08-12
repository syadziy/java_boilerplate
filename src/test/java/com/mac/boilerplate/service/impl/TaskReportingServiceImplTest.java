package com.mac.boilerplate.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.mac.boilerplate.config.properties.TaskProperties;
import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.TaskAuditResponse;
import com.mac.boilerplate.entities.dto.TaskDashboardResponse;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.boilerplate.entities.dto.TaskSearchCriteria;
import com.mac.boilerplate.entities.dto.TaskSearchResult;
import com.mac.boilerplate.entities.mapper.TaskProjectionMapper;
import com.mac.boilerplate.entities.model.TaskAuditDocument;
import com.mac.boilerplate.entities.model.TaskSearchDocument;
import com.mac.boilerplate.entities.model.TaskSearchRow;
import com.mac.boilerplate.repository.TaskAuditRepository;
import com.mac.boilerplate.repository.TaskReportCacheRepository;
import com.mac.boilerplate.repository.TaskSearchIndexRepository;
import com.mac.boilerplate.repository.TaskSearchRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

class TaskReportingServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-08-09T00:00:00Z");

    private TaskSearchRepository searchRepository;
    private TaskSearchIndexRepository searchIndexRepository;
    private TaskAuditRepository auditRepository;
    private TaskReportCacheRepository cacheRepository;
    private TaskReportingServiceImpl service;

    @BeforeEach
    void setUp() {
        searchRepository = mock(TaskSearchRepository.class);
        searchIndexRepository = mock(TaskSearchIndexRepository.class);
        auditRepository = mock(TaskAuditRepository.class);
        cacheRepository = mock(TaskReportCacheRepository.class);
        service = new TaskReportingServiceImpl(
                searchRepository,
                searchIndexRepository,
                auditRepository,
                cacheRepository,
                new TaskProjectionMapper(),
                new TaskProperties(
                        new TaskProperties.Cache(Duration.ofMinutes(10)),
                        new TaskProperties.Cleanup(Duration.ofDays(30)),
                        new TaskProperties.Reporting(Duration.ofMinutes(5), Duration.ofDays(30),
                                "task-search", "task_audit")),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void searchUsesCacheFirst() {
        TaskSearchCriteria criteria = new TaskSearchCriteria("Learn", TaskStatus.PENDING, null, null, 10, 0);
        TaskSearchResult cached = new TaskSearchResult(List.of(task()), 1, 10, 0);
        when(cacheRepository.findSearch(any())).thenReturn(Optional.of(cached));

        assertThat(service.search(criteria)).isSameAs(cached);
        verifyNoInteractions(searchRepository);
    }

    @Test
    void searchCachesDatabaseResultWhenMissing() {
        TaskSearchCriteria criteria = new TaskSearchCriteria("Learn", TaskStatus.PENDING, null, null, 10, 0);
        TaskSearchResult result = new TaskSearchResult(List.of(task()), 1, 10, 0);
        when(cacheRepository.findSearch(any())).thenReturn(Optional.empty());
        when(searchRepository.search(any())).thenReturn(result);

        assertThat(service.search(criteria)).isEqualTo(result);
        verify(cacheRepository).putSearch(any(), eq(result));
    }

    @Test
    void searchIndexFiltersDocumentsAndAppliesLimit() {
        UUID id = UUID.randomUUID();
        when(searchIndexRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("learn", "learn"))
                .thenReturn(List.of(
                        document(id, "Learn Redis", TaskStatus.PENDING),
                        document(UUID.randomUUID(), "Learn Kafka", TaskStatus.COMPLETED)));

        TaskSearchResult result = service.searchIndex("learn", TaskStatus.PENDING, 1);

        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().id()).isEqualTo(id);
    }

    @Test
    void dashboardAggregatesAndCaches() {
        UUID completedId = UUID.randomUUID();
        when(cacheRepository.findDashboard(any(), any())).thenReturn(Optional.empty());
        when(searchRepository.findWithin(any(), any())).thenReturn(List.of(
                new TaskSearchRow(UUID.randomUUID(), "Pending", null, TaskStatus.PENDING, NOW, null, 0),
                new TaskSearchRow(completedId, "Completed", null, TaskStatus.COMPLETED, NOW, NOW.plusSeconds(120), 0)));

        TaskDashboardResponse response = service.dashboard(null, null);

        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.countsByStatus()).containsEntry(TaskStatus.PENDING, 1L);
        assertThat(response.countsByStatus()).containsEntry(TaskStatus.COMPLETED, 1L);
        assertThat(response.averageCompletionMinutes()).isEqualTo(2.0);
        verify(cacheRepository).putDashboard(any(), any(), eq(response));
    }

    @Test
    void auditTrailMapsDocumentsAndFallsBackToEmptyOnFailure() {
        UUID taskId = UUID.randomUUID();
        when(auditRepository.findByTaskIdOrderByOccurredAtDesc(taskId)).thenReturn(List.of(
                audit(taskId, "TASK_CREATED"),
                audit(taskId, "TASK_COMPLETED")));

        List<TaskAuditResponse> audit = service.auditTrail(taskId);

        assertThat(audit).hasSize(2);
        assertThat(audit.getFirst().taskId()).isEqualTo(taskId);

        when(auditRepository.findByTaskIdOrderByOccurredAtDesc(taskId)).thenThrow(new RuntimeException("mongo down"));
        assertThat(service.auditTrail(taskId)).isEmpty();
    }

    private static TaskResponse task() {
        return new TaskResponse(UUID.randomUUID(), "A", null, TaskStatus.PENDING, NOW, null);
    }

    private static TaskSearchDocument document(UUID id, String title, TaskStatus status) {
        return new TaskSearchDocument(id.toString(), id, title, "desc", status, NOW, null);
    }

    private static TaskAuditDocument audit(UUID taskId, String eventType) {
        return new TaskAuditDocument(UUID.randomUUID().toString(), taskId, eventType, "A", null,
                TaskStatus.PENDING, NOW);
    }
}
