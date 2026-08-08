package com.mac.boilerplate.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.TaskResponse;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

class RedisTaskCacheRepositoryTest {

    @Test
    void performsCacheOperations() {
        CacheManager manager = mock(CacheManager.class);
        Cache cache = mock(Cache.class);
        Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
        TaskResponse task = task();
        when(manager.getCache("tasks")).thenReturn(cache);
        when(cache.get(task.id().toString())).thenReturn(wrapper, null);
        when(wrapper.get()).thenReturn(task);
        var repository = new RedisTaskCacheRepository(manager);

        assertThat(repository.find(task.id())).contains(task);
        assertThat(repository.find(task.id())).isEmpty();
        repository.put(task);
        repository.evict(task.id());
        repository.clear();

        verify(cache).put(task.id().toString(), task);
        verify(cache).evict(task.id().toString());
        verify(cache).clear();
    }

    @Test
    void redisFailuresAreBestEffort() {
        CacheManager manager = mock(CacheManager.class);
        when(manager.getCache("tasks")).thenReturn(null);
        var repository = new RedisTaskCacheRepository(manager);
        TaskResponse task = task();

        assertThat(repository.find(task.id())).isEmpty();
        repository.put(task);
        repository.evict(task.id());
        repository.clear();
    }

    private static TaskResponse task() {
        return new TaskResponse(UUID.randomUUID(), "A", null, TaskStatus.PENDING, Instant.EPOCH, null);
    }
}
