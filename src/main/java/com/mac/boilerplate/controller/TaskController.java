package com.mac.boilerplate.controller;

import com.mac.boilerplate.entities.dto.CreateTaskRequest;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.boilerplate.service.TaskService;
import com.mac.sdk_util.entities.dto.ResponseDTO;
import com.mac.sdk_util.helper.ResponseHelper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<TaskResponse>> create(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseHelper.httpCreated(response, location);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<TaskResponse>> findById(@PathVariable UUID id) {
        return ResponseHelper.httpOK(service.findById(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ResponseDTO<TaskResponse>> complete(@PathVariable UUID id) {
        return ResponseHelper.httpOK(service.complete(id));
    }
}
