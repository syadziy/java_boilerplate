package com.mac.boilerplate.service;

import com.mac.boilerplate.entities.dto.CreateTaskRequest;
import com.mac.boilerplate.entities.dto.TaskResponse;
import java.util.UUID;

public interface TaskService {

    TaskResponse create(CreateTaskRequest request);

    TaskResponse findById(UUID id);

    TaskResponse complete(UUID id);
}
