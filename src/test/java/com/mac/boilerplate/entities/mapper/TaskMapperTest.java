package com.mac.boilerplate.entities.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.CreateTaskRequest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TaskMapperTest {

    private final TaskMapper mapper = new TaskMapper();

    @Test
    void mapsAndNormalizesRequest() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-08-09T00:00:00Z");

        var task = mapper.toModel(new CreateTaskRequest("  Learn Redis  ", "  cache aside  "), id, now);
        var response = mapper.toResponse(task);

        assertThat(task.title()).isEqualTo("Learn Redis");
        assertThat(task.description()).isEqualTo("cache aside");
        assertThat(task.status()).isEqualTo(TaskStatus.PENDING);
        assertThat(response.id()).isEqualTo(id);
    }

    @Test
    void convertsBlankOrNullDescriptionToNull() {
        Instant now = Instant.EPOCH;
        assertThat(mapper.toModel(new CreateTaskRequest("A", "  "), UUID.randomUUID(), now).description()).isNull();
        assertThat(mapper.toModel(new CreateTaskRequest("A", null), UUID.randomUUID(), now).description()).isNull();
    }
}
