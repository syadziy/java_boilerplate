package com.mac.boilerplate.service;

import com.mac.boilerplate.entities.dto.TaskResponse;

public interface TaskProjectionSyncService {

    void syncTask(TaskResponse response, String eventType);
}
