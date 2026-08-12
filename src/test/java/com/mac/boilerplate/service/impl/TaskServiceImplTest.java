package com.mac.boilerplate.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.CreateTaskRequest;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.boilerplate.entities.mapper.TaskMapper;
import com.mac.boilerplate.entities.model.Task;
import com.mac.boilerplate.repository.TaskCacheRepository;
import com.mac.boilerplate.repository.TaskRepository;
import com.mac.boilerplate.service.TaskProjectionSyncService;
import com.mac.boilerplate.utils.exception.InvalidTaskStateException;
import com.mac.sdk_util.exception.ResourceNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-08-09T00:00:00Z");
    private TaskRepository repository;
    private TaskCacheRepository cache;
    private TaskProjectionSyncService syncService;
    private TaskServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(TaskRepository.class);
        cache = mock(TaskCacheRepository.class);
        syncService = mock(TaskProjectionSyncService.class);
        service = new TaskServiceImpl(
                repository, cache, syncService, new TaskMapper(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsAndCachesTask() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse result = service.create(new CreateTaskRequest("  title  ", "description"));

        assertThat(result.title()).isEqualTo("title");
        assertThat(result.createdAt()).isEqualTo(NOW);
        verify(cache).put(result);
        verify(syncService).syncTask(result, "TASK_CREATED");
    }

    @Test
    void readsCacheWithoutDatabase() {
        TaskResponse cached = response(TaskStatus.PENDING, null);
        when(cache.find(cached.id())).thenReturn(Optional.of(cached));

        assertThat(service.findById(cached.id())).isSameAs(cached);
        verifyNoInteractions(repository);
    }

    @Test
    void databaseHitPopulatesCache() {
        TaskResponse expected = response(TaskStatus.PENDING, null);
        when(cache.find(expected.id())).thenReturn(Optional.empty());
        when(repository.findById(expected.id())).thenReturn(Optional.of(model(expected)));

        assertThat(service.findById(expected.id())).isEqualTo(expected);
        verify(cache).put(expected);
    }

    @Test
    void missingTaskThrowsNotFound() {
        UUID id = UUID.randomUUID();
        when(cache.find(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void completesPendingTaskAndEvictsCache() {
        TaskResponse completed = response(TaskStatus.COMPLETED, NOW);
        when(repository.markCompleted(completed.id(), NOW)).thenReturn(true);
        when(cache.find(completed.id())).thenReturn(Optional.empty());
        when(repository.findById(completed.id())).thenReturn(Optional.of(model(completed)));

        assertThat(service.complete(completed.id()).status()).isEqualTo(TaskStatus.COMPLETED);
        verify(cache).evict(completed.id());
        verify(syncService).syncTask(any(), eq("TASK_COMPLETED"));
    }

    @Test
    void rejectsMissingOrAlreadyCompletedTask() {
        UUID id = UUID.randomUUID();
        when(repository.markCompleted(id, NOW)).thenReturn(false);
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.complete(id)).isInstanceOf(ResourceNotFoundException.class);

        when(repository.findById(id)).thenReturn(Optional.of(new Task(id, "A", null,
                TaskStatus.COMPLETED, NOW, NOW)));
        assertThatThrownBy(() -> service.complete(id)).isInstanceOf(InvalidTaskStateException.class);
    }

    private static TaskResponse response(TaskStatus status, Instant completedAt) {
        return new TaskResponse(UUID.randomUUID(), "A", "B", status, NOW, completedAt);
    }

    private static Task model(TaskResponse value) {
        return new Task(value.id(), value.title(), value.description(), value.status(),
                value.createdAt(), value.completedAt());
    }
}
