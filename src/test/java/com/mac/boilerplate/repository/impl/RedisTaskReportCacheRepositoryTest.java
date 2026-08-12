package com.mac.boilerplate.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.mac.boilerplate.config.properties.TaskProperties;
import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.TaskDashboardResponse;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.boilerplate.entities.dto.TaskSearchCriteria;
import com.mac.boilerplate.entities.dto.TaskSearchResult;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisTaskReportCacheRepositoryTest {

    @Test
    void readsAndWritesSearchAndDashboardEntries() throws Exception {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> operations = mock(ValueOperations.class);
        tools.jackson.databind.ObjectMapper objectMapper = mock(tools.jackson.databind.ObjectMapper.class);
        when(redisTemplate.opsForValue()).thenReturn(operations);
        var properties = new TaskProperties(
                new TaskProperties.Cache(Duration.ofMinutes(10)),
                new TaskProperties.Cleanup(Duration.ofDays(30)),
                new TaskProperties.Reporting(Duration.ofMinutes(5), Duration.ofDays(30), "task-search",
                        "task_audit"));
        var repository = new RedisTaskReportCacheRepository(redisTemplate, properties, objectMapper);
        TaskSearchCriteria criteria = new TaskSearchCriteria("learn", TaskStatus.PENDING, Instant.EPOCH,
                Instant.EPOCH.plusSeconds(60), 10, 0);
        TaskSearchResult searchResult = new TaskSearchResult(List.of(task()), 1, 10, 0);
        TaskDashboardResponse dashboard = new TaskDashboardResponse(
                Instant.EPOCH, Instant.EPOCH.plusSeconds(60), 1, Map.of(TaskStatus.PENDING, 1L), 0.0, List.of(task()));

        when(operations.get(contains("task:report:search"))).thenReturn("{\"items\":[]}");
        when(objectMapper.readValue(eq("{\"items\":[]}"), eq(TaskSearchResult.class))).thenReturn(searchResult);
        when(objectMapper.writeValueAsString(searchResult)).thenReturn("{\"items\":[]}");
        when(objectMapper.writeValueAsString(dashboard)).thenReturn("{\"totalCount\":1}");

        assertThat(repository.findSearch(criteria)).contains(searchResult);
        repository.putSearch(criteria, searchResult);
        repository.putDashboard(Instant.EPOCH, Instant.EPOCH.plusSeconds(60), dashboard);
    }

    @Test
    void bestEffortReadReturnsEmptyOnFailure() throws Exception {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> operations = mock(ValueOperations.class);
        tools.jackson.databind.ObjectMapper objectMapper = mock(tools.jackson.databind.ObjectMapper.class);
        when(redisTemplate.opsForValue()).thenReturn(operations);
        when(operations.get(anyString())).thenThrow(new RuntimeException("redis down"));
        var properties = new TaskProperties(
                new TaskProperties.Cache(Duration.ofMinutes(10)),
                new TaskProperties.Cleanup(Duration.ofDays(30)),
                new TaskProperties.Reporting(Duration.ofMinutes(5), Duration.ofDays(30), "task-search",
                        "task_audit"));
        var repository = new RedisTaskReportCacheRepository(redisTemplate, properties, objectMapper);

        assertThat(repository.findDashboard(Instant.EPOCH, Instant.EPOCH.plusSeconds(60))).isEmpty();
    }

    private static TaskResponse task() {
        return new TaskResponse(UUID.randomUUID(), "A", null, TaskStatus.PENDING, Instant.EPOCH, null);
    }
}
