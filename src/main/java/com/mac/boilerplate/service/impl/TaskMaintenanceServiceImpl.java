package com.mac.boilerplate.service.impl;

import com.mac.boilerplate.config.properties.TaskProperties;
import com.mac.boilerplate.repository.TaskCacheRepository;
import com.mac.boilerplate.repository.TaskRepository;
import com.mac.boilerplate.service.TaskMaintenanceService;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskMaintenanceServiceImpl implements TaskMaintenanceService {

    private final TaskRepository repository;
    private final TaskCacheRepository cacheRepository;
    private final TaskProperties properties;
    private final Clock clock;

    public TaskMaintenanceServiceImpl(
            TaskRepository repository,
            TaskCacheRepository cacheRepository,
            TaskProperties properties,
            Clock clock) {
        this.repository = repository;
        this.cacheRepository = cacheRepository;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    @Transactional
    public int removeExpiredCompletedTasks() {
        int deleted = repository.deleteCompletedBefore(clock.instant().minus(properties.cleanup().retention()));
        if (deleted > 0) {
            cacheRepository.clear();
        }
        return deleted;
    }
}
