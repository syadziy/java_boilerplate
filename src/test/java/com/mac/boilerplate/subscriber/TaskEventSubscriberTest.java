package com.mac.boilerplate.subscriber;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.CreateTaskEvent;
import com.mac.boilerplate.entities.dto.CreateTaskRequest;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.boilerplate.service.TaskService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

class TaskEventSubscriberTest {

    @Test
    void validatesAndCreatesTaskWithEveryTraceFallback() {
        TaskService service = mock(TaskService.class);
        Validator validator = mock(Validator.class);
        when(validator.validate(any(CreateTaskEvent.class))).thenReturn(Collections.emptySet());
        when(service.create(any())).thenReturn(new TaskResponse(
                UUID.randomUUID(), "A", null, TaskStatus.PENDING, Instant.EPOCH, null));
        TaskEventSubscriber subscriber = new TaskEventSubscriber(service, validator);

        subscriber.consume(record("key", new CreateTaskEvent("event", "A", null, "trace")));
        subscriber.consume(record("key", new CreateTaskEvent("event", "A", null, " ")));
        subscriber.consume(record(" ", new CreateTaskEvent("event", "A", null, null)));
        subscriber.consume(record(null, new CreateTaskEvent(" ", "A", null, null)));

        verify(service, times(4)).create(any(CreateTaskRequest.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsInvalidKafkaPayload() {
        TaskService service = mock(TaskService.class);
        Validator validator = mock(Validator.class);
        ConstraintViolation<CreateTaskEvent> violation = mock(ConstraintViolation.class);
        when(validator.validate(any(CreateTaskEvent.class))).thenReturn(Set.of(violation));
        TaskEventSubscriber subscriber = new TaskEventSubscriber(service, validator);

        assertThatThrownBy(() -> subscriber.consume(record("key",
                new CreateTaskEvent("", "", null, null))))
                .isInstanceOf(ConstraintViolationException.class);
        verifyNoInteractions(service);
    }

    private static ConsumerRecord<String, CreateTaskEvent> record(String key, CreateTaskEvent event) {
        return new ConsumerRecord<>("task.create", 1, 2L, key, event);
    }
}
