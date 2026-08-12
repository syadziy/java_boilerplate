package com.mac.boilerplate.repository.impl;

import com.mac.boilerplate.config.properties.TaskProperties;
import com.mac.boilerplate.entities.dto.TaskDashboardResponse;
import com.mac.boilerplate.entities.dto.TaskSearchCriteria;
import com.mac.boilerplate.entities.dto.TaskSearchResult;
import com.mac.boilerplate.repository.TaskReportCacheRepository;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RedisTaskReportCacheRepository implements TaskReportCacheRepository {

    private static final Logger LOG = LoggerFactory.getLogger(RedisTaskReportCacheRepository.class);
    private static final String SEARCH_PREFIX = "task:report:search:";
    private static final String DASHBOARD_PREFIX = "task:report:dashboard:";

    private final StringRedisTemplate redisTemplate;
    private final TaskProperties properties;
    private final tools.jackson.databind.ObjectMapper objectMapper;

    public RedisTaskReportCacheRepository(
            StringRedisTemplate redisTemplate,
            TaskProperties properties,
            tools.jackson.databind.ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<TaskSearchResult> findSearch(TaskSearchCriteria criteria) {
        return read(SEARCH_PREFIX + criteria.cacheKey(), TaskSearchResult.class);
    }

    @Override
    public void putSearch(TaskSearchCriteria criteria, TaskSearchResult result) {
        write(SEARCH_PREFIX + criteria.cacheKey(), result);
    }

    @Override
    public Optional<TaskDashboardResponse> findDashboard(Instant from, Instant to) {
        return read(DASHBOARD_PREFIX + cacheKey(from, to), TaskDashboardResponse.class);
    }

    @Override
    public void putDashboard(Instant from, Instant to, TaskDashboardResponse result) {
        write(DASHBOARD_PREFIX + cacheKey(from, to), result);
    }

    private <T> Optional<T> read(String key, Class<T> type) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null || value.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(value, type));
        } catch (Exception exception) {
            logFailure("read", key, exception);
            return Optional.empty();
        }
    }

    private void write(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), properties.reporting().cacheTtl());
        } catch (Exception exception) {
            logFailure("write", key, exception);
        }
    }

    private static String cacheKey(Instant from, Instant to) {
        return String.valueOf(from) + "|" + to;
    }

    private void logFailure(String action, String key, Exception exception) {
        LOG.warn("Reporting cache {} failed for key {}", action, key, exception);
    }
}
