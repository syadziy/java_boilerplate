package com.mac.boilerplate.subscriber;

import com.mac.boilerplate.entities.constant.TaskLogFields;
import com.mac.boilerplate.entities.dto.CreateTaskEvent;
import com.mac.boilerplate.entities.dto.CreateTaskRequest;
import com.mac.boilerplate.service.TaskService;
import com.mac.sdk_util.entities.constant.LogFields;
import com.mac.sdk_util.utils.StructuredLog;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "task.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class TaskEventSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(TaskEventSubscriber.class);
    private final TaskService service;
    private final Validator validator;

    public TaskEventSubscriber(TaskService service, Validator validator) {
        this.service = service;
        this.validator = validator;
    }

    @KafkaListener(topics = "${task.kafka.create-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, CreateTaskEvent> record) {
        CreateTaskEvent event = record.value();
        var violations = validator.validate(event);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        String traceId = firstNonBlank(event.traceId(), record.key(), event.eventId(), UUID.randomUUID().toString());
        Map<String, String> mdc = Map.of(
                LogFields.TRACE_ID, traceId,
                LogFields.EVENT_DATASET, "boilerplate.kafka");
        StructuredLog.withMdc(mdc, () -> {
            var task = service.create(new CreateTaskRequest(event.title(), event.description()));
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put(LogFields.EVENT_ACTION, "consumeCreateTask");
            fields.put(LogFields.EVENT_OUTCOME, LogFields.OUTCOME_SUCCESS);
            fields.put(LogFields.EVENT_DATASET, "boilerplate.kafka");
            fields.put(TaskLogFields.TASK_ID, task.id());
            fields.put("kafka.topic", record.topic());
            fields.put("kafka.partition", record.partition());
            fields.put("kafka.offset", record.offset());
            StructuredLog.info(LOG, "Task event consumed", fields);
        });
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        throw new IllegalArgumentException("At least one trace identifier is required");
    }
}
