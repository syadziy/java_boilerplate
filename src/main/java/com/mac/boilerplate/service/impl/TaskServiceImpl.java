package com.mac.boilerplate.service.impl;

import com.mac.boilerplate.entities.dto.CreateTaskRequest;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.boilerplate.entities.mapper.TaskMapper;
import com.mac.boilerplate.repository.TaskCacheRepository;
import com.mac.boilerplate.repository.TaskRepository;
import com.mac.boilerplate.service.TaskService;
import com.mac.boilerplate.service.TaskProjectionSyncService;
import com.mac.boilerplate.utils.exception.InvalidTaskStateException;
import com.mac.sdk_util.exception.ResourceNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repository;
    private final TaskCacheRepository cacheRepository;
    private final TaskProjectionSyncService projectionSyncService;
    private final TaskMapper mapper;
    private final Clock clock;

    public TaskServiceImpl(
            TaskRepository repository,
            TaskCacheRepository cacheRepository,
            TaskProjectionSyncService projectionSyncService,
            TaskMapper mapper,
            Clock clock) {
        this.repository = repository;
        this.cacheRepository = cacheRepository;
        this.projectionSyncService = projectionSyncService;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        var task = mapper.toModel(request, UUID.randomUUID(), clock.instant());
        var response = mapper.toResponse(repository.save(task));
        cacheRepository.put(response);
        projectionSyncService.syncTask(response, "TASK_CREATED");
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse findById(UUID id) {
        return cacheRepository.find(id).orElseGet(() -> {
            var task = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Task was not found"));
            var response = mapper.toResponse(task);
            cacheRepository.put(response);
            return response;
        });
    }

    @Override
    @Transactional
    public TaskResponse complete(UUID id) {
        Instant completedAt = clock.instant();
        if (!repository.markCompleted(id, completedAt)) {
            if (repository.findById(id).isEmpty()) {
                throw new ResourceNotFoundException("Task was not found");
            }
            throw new InvalidTaskStateException("Only pending tasks can be completed");
        }
        cacheRepository.evict(id);
        TaskResponse response = findById(id);
        projectionSyncService.syncTask(response, "TASK_COMPLETED");
        return response;
    }
}
