package com.mac.boilerplate.repository;

import com.mac.boilerplate.entities.dto.TaskResponse;
import java.util.Optional;
import java.util.UUID;

public interface TaskCacheRepository {

    Optional<TaskResponse> find(UUID id);

    void put(TaskResponse task);

    void evict(UUID id);

    void clear();
}
