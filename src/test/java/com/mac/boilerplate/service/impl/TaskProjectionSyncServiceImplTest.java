package com.mac.boilerplate.service.impl;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.boilerplate.entities.mapper.TaskProjectionMapper;
import com.mac.boilerplate.repository.TaskAuditRepository;
import com.mac.boilerplate.repository.TaskSearchIndexRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TaskProjectionSyncServiceImplTest {

    @Test
    void syncsTaskToElasticAndMongoBestEffort() {
        TaskSearchIndexRepository searchIndexRepository = mock(TaskSearchIndexRepository.class);
        TaskAuditRepository auditRepository = mock(TaskAuditRepository.class);
        TaskProjectionSyncServiceImpl service = new TaskProjectionSyncServiceImpl(
                searchIndexRepository, auditRepository, new TaskProjectionMapper());
        TaskResponse response = new TaskResponse(
                UUID.randomUUID(), "A", "B", TaskStatus.PENDING, Instant.EPOCH, null);

        assertThatNoException().isThrownBy(() -> service.syncTask(response, "TASK_CREATED"));
        verify(searchIndexRepository).save(any());
        verify(auditRepository).save(any());
    }

    @Test
    void ignoresProjectionFailures() {
        TaskSearchIndexRepository searchIndexRepository = mock(TaskSearchIndexRepository.class);
        TaskAuditRepository auditRepository = mock(TaskAuditRepository.class);
        when(searchIndexRepository.save(any())).thenThrow(new RuntimeException("elastic down"));
        TaskProjectionSyncServiceImpl service = new TaskProjectionSyncServiceImpl(
                searchIndexRepository, auditRepository, new TaskProjectionMapper());

        assertThatNoException().isThrownBy(() -> service.syncTask(
                new TaskResponse(UUID.randomUUID(), "A", null, TaskStatus.PENDING, Instant.EPOCH, null),
                "TASK_CREATED"));
    }
}
