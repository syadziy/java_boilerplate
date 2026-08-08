package com.mac.boilerplate.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.CreateTaskRequest;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.boilerplate.service.TaskService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.mock.web.MockHttpServletRequest;

class TaskControllerTest {

    @Test
    void delegatesAllEndpointsAndBuildsLocation() {
        TaskService service = mock(TaskService.class);
        TaskController controller = new TaskController(service);
        UUID id = UUID.randomUUID();
        TaskResponse response = new TaskResponse(id, "A", null, TaskStatus.PENDING, Instant.EPOCH, null);
        when(service.create(any())).thenReturn(response);
        when(service.findById(id)).thenReturn(response);
        when(service.complete(id)).thenReturn(response);
        var servletRequest = new MockHttpServletRequest("POST", "/api/v1/tasks");
        ServletRequestAttributes attributes = new ServletRequestAttributes(servletRequest);
        RequestContextHolder.setRequestAttributes(attributes);
        try {
            var created = controller.create(new CreateTaskRequest("A", null));
            assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(created.getHeaders().getLocation().toString()).endsWith("/api/v1/tasks/" + id);
            assertThat(controller.findById(id).getBody().getData()).isEqualTo(response);
            assertThat(controller.complete(id).getBody().getData()).isEqualTo(response);
        } finally {
            RequestContextHolder.resetRequestAttributes();
            attributes.requestCompleted();
        }
    }
}
