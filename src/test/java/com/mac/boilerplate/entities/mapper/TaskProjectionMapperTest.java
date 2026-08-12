package com.mac.boilerplate.entities.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.boilerplate.entities.model.TaskAuditDocument;
import com.mac.boilerplate.entities.model.TaskSearchRow;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TaskProjectionMapperTest {

    private final TaskProjectionMapper mapper = new TaskProjectionMapper();

    @Test
    void mapsProjectionDocumentsAndResponses() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-08-09T00:00:00Z");
        TaskResponse response = new TaskResponse(id, "A", "B", TaskStatus.COMPLETED, now, now);

        var searchDocument = mapper.toSearchDocument(response);
        TaskAuditDocument auditDocument = mapper.toAuditDocument(response, "TASK_CREATED");
        var mappedResponse = mapper.toResponse(new TaskSearchRow(id, "A", "B", TaskStatus.COMPLETED, now, now, 1));

        assertThat(searchDocument.taskId()).isEqualTo(id);
        assertThat(searchDocument.title()).isEqualTo("A");
        assertThat(auditDocument.status()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(mappedResponse.completedAt()).isEqualTo(now);
    }
}
