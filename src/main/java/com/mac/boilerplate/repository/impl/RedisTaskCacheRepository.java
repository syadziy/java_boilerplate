package com.mac.boilerplate.repository.impl;

import com.mac.boilerplate.entities.constant.TaskLogFields;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.boilerplate.repository.TaskCacheRepository;
import com.mac.sdk_util.entities.constant.LogFields;
import com.mac.sdk_util.utils.StructuredLog;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

@Repository
public class RedisTaskCacheRepository implements TaskCacheRepository {

    public static final String CACHE_NAME = "tasks";
    private static final Logger LOG = LoggerFactory.getLogger(RedisTaskCacheRepository.class);

    private final CacheManager cacheManager;

    public RedisTaskCacheRepository(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Optional<TaskResponse> find(UUID id) {
        try {
            Cache.ValueWrapper value = cache().get(id.toString());
            return value == null ? Optional.empty() : Optional.of((TaskResponse) value.get());
        } catch (RuntimeException exception) {
            logUnavailable("readTaskCache", exception);
            return Optional.empty();
        }
    }

    @Override
    public void put(TaskResponse task) {
        safely("writeTaskCache", () -> cache().put(task.id().toString(), task));
    }

    @Override
    public void evict(UUID id) {
        safely("evictTaskCache", () -> cache().evict(id.toString()));
    }

    @Override
    public void clear() {
        safely("clearTaskCache", () -> cache().clear());
    }

    private Cache cache() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            throw new IllegalStateException("Task cache is not configured");
        }
        return cache;
    }

    private void safely(String action, Runnable operation) {
        try {
            operation.run();
        } catch (RuntimeException exception) {
            logUnavailable(action, exception);
        }
    }

    private void logUnavailable(String action, RuntimeException exception) {
        StructuredLog.warn(LOG, "Redis operation failed; database remains available", Map.of(
                LogFields.EVENT_ACTION, action,
                LogFields.EVENT_OUTCOME, LogFields.OUTCOME_FAILURE,
                LogFields.EVENT_DATASET, "boilerplate.redis",
                TaskLogFields.CACHE_RESULT, "unavailable",
                "error.type", exception.getClass().getSimpleName()));
    }
}
