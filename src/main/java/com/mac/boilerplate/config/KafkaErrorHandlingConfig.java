package com.mac.boilerplate.config;

import com.mac.boilerplate.utils.handler.AsyncExceptionHandler;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
public class KafkaErrorHandlingConfig {

    @Bean
    CommonErrorHandler kafkaErrorHandler(
            AsyncExceptionHandler exceptionHandler,
            KafkaOperations<Object, Object> kafkaOperations,
            @Value("${task.kafka.dead-letter-topic}") String deadLetterTopic,
            @Value("${task.kafka.retry-interval-ms:1000}") long retryIntervalMs,
            @Value("${task.kafka.max-retries:2}") long maxRetries) {
        var recoverer = new DeadLetterPublishingRecoverer(
                kafkaOperations,
                (record, exception) -> new TopicPartition(deadLetterTopic, record.partition()));
        var handler = new DefaultErrorHandler((record, exception) -> {
            exceptionHandler.handle(
                    record.key() == null ? null : record.key().toString(),
                    "boilerplate.kafka",
                    "kafka-listener",
                    "consumeCreateTask",
                    Map.of(
                            "kafka.topic", record.topic(),
                            "kafka.partition", record.partition(),
                            "kafka.offset", record.offset(),
                            "kafka.dlt.topic", deadLetterTopic),
                    exception);
            recoverer.accept(record, exception);
        }, new FixedBackOff(retryIntervalMs, maxRetries));
        handler.addNotRetryableExceptions(ConstraintViolationException.class, IllegalArgumentException.class);
        return handler;
    }
}
