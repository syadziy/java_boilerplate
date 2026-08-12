package com.mac.boilerplate.service.impl;

import com.mac.boilerplate.config.properties.TaskProperties;
import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.TaskAuditResponse;
import com.mac.boilerplate.entities.dto.TaskDashboardResponse;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.boilerplate.entities.dto.TaskSearchCriteria;
import com.mac.boilerplate.entities.dto.TaskSearchResult;
import com.mac.boilerplate.entities.mapper.TaskProjectionMapper;
import com.mac.boilerplate.entities.model.TaskSearchDocument;
import com.mac.boilerplate.entities.model.TaskSearchRow;
import com.mac.boilerplate.repository.TaskAuditRepository;
import com.mac.boilerplate.repository.TaskReportCacheRepository;
import com.mac.boilerplate.repository.TaskSearchIndexRepository;
import com.mac.boilerplate.repository.TaskSearchRepository;
import com.mac.boilerplate.service.TaskReportingService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TaskReportingServiceImpl implements TaskReportingService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskReportingServiceImpl.class);

    private final TaskSearchRepository searchRepository;
    private final TaskSearchIndexRepository searchIndexRepository;
    private final TaskAuditRepository auditRepository;
    private final TaskReportCacheRepository cacheRepository;
    private final TaskProjectionMapper mapper;
    private final TaskProperties properties;
    private final Clock clock;

    public TaskReportingServiceImpl(
            TaskSearchRepository searchRepository,
            TaskSearchIndexRepository searchIndexRepository,
            TaskAuditRepository auditRepository,
            TaskReportCacheRepository cacheRepository,
            TaskProjectionMapper mapper,
            TaskProperties properties,
            Clock clock) {
        this.searchRepository = searchRepository;
        this.searchIndexRepository = searchIndexRepository;
        this.auditRepository = auditRepository;
        this.cacheRepository = cacheRepository;
        this.mapper = mapper;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public TaskSearchResult search(TaskSearchCriteria criteria) {
        TaskSearchCriteria normalized = normalize(criteria);
        return cacheRepository.findSearch(normalized).orElseGet(() -> {
            TaskSearchResult result = searchRepository.search(normalized);
            cacheRepository.putSearch(normalized, result);
            return result;
        });
    }

    @Override
    public TaskSearchResult searchIndex(String query, TaskStatus status, int limit) {
        String normalizedQuery = normalizeQuery(query);
        int safeLimit = Math.max(1, limit);
        List<TaskSearchDocument> documents;
        try {
            if (normalizedQuery.isBlank() && status == null) {
                documents = StreamSupport.stream(searchIndexRepository.findAll().spliterator(), false).toList();
            } else if (normalizedQuery.isBlank()) {
                documents = searchIndexRepository.findByStatus(status);
            } else {
                documents = searchIndexRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        normalizedQuery, normalizedQuery);
                if (status != null) {
                    documents = documents.stream()
                            .filter(document -> document.status() == status)
                            .toList();
                }
            }
        } catch (RuntimeException exception) {
            LOG.warn("Elastic search fallback returned empty result", exception);
            documents = List.of();
        }

        List<TaskResponse> items = documents.stream()
                .limit(safeLimit)
                .map(document -> new TaskResponse(
                        document.taskId(),
                        document.title(),
                        document.description(),
                        document.status(),
                        document.createdAt(),
                        document.completedAt()))
                .toList();
        return new TaskSearchResult(items, documents.size(), safeLimit, 0);
    }

    @Override
    public TaskDashboardResponse dashboard(Instant from, Instant to) {
        Instant normalizedTo = to == null ? clock.instant() : to;
        Instant normalizedFrom = from == null
                ? normalizedTo.minus(properties.reporting().defaultWindow())
                : from;

        TaskDashboardResponse cached = cacheRepository.findDashboard(normalizedFrom, normalizedTo).orElse(null);
        if (cached != null) {
            return cached;
        }

        List<TaskSearchRow> rows = searchRepository.findWithin(normalizedFrom, normalizedTo);
        Map<TaskStatus, Long> countsByStatus = rows.stream().collect(
                Collectors.groupingBy(TaskSearchRow::status, Collectors.counting()));
        double averageCompletionMinutes = rows.stream()
                .filter(row -> row.completedAt() != null)
                .mapToDouble(row -> Duration.between(row.createdAt(), row.completedAt()).toMinutes())
                .average()
                .orElse(0d);
        List<TaskResponse> recentTasks = rows.stream()
                .limit(5)
                .map(mapper::toResponse)
                .toList();

        TaskDashboardResponse response = new TaskDashboardResponse(
                normalizedFrom,
                normalizedTo,
                rows.size(),
                countsByStatus,
                averageCompletionMinutes,
                recentTasks);
        cacheRepository.putDashboard(normalizedFrom, normalizedTo, response);
        return response;
    }

    @Override
    public List<TaskAuditResponse> auditTrail(UUID taskId) {
        try {
            return auditRepository.findByTaskIdOrderByOccurredAtDesc(taskId).stream()
                    .map(mapper::toResponse)
                    .toList();
        } catch (RuntimeException exception) {
            LOG.warn("Audit trail read failed for task {}", taskId, exception);
            return List.of();
        }
    }

    private TaskSearchCriteria normalize(TaskSearchCriteria criteria) {
        Instant normalizedTo = criteria.to() == null ? clock.instant() : criteria.to();
        Instant normalizedFrom = criteria.from() == null
                ? normalizedTo.minus(properties.reporting().defaultWindow())
                : criteria.from();
        return new TaskSearchCriteria(
                normalizeQuery(criteria.query()),
                criteria.status(),
                normalizedFrom,
                normalizedTo,
                Math.max(1, criteria.limit()),
                Math.max(0, criteria.offset()));
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    }
}
