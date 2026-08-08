package com.mac.boilerplate.repository;

import com.mac.boilerplate.entities.model.Task;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {

    Task save(Task task);

    Optional<Task> findById(UUID id);

    boolean markCompleted(UUID id, Instant completedAt);

    int deleteCompletedBefore(Instant cutoff);
}
