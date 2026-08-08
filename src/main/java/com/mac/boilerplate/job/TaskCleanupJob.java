package com.mac.boilerplate.job;

import com.mac.boilerplate.service.TaskMaintenanceService;
import com.mac.boilerplate.utils.handler.AsyncExceptionHandler;
import com.mac.sdk_util.entities.constant.LogFields;
import com.mac.sdk_util.utils.StructuredLog;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "task.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class TaskCleanupJob {

    private static final Logger LOG = LoggerFactory.getLogger(TaskCleanupJob.class);
    private final TaskMaintenanceService service;
    private final AsyncExceptionHandler exceptionHandler;

    public TaskCleanupJob(TaskMaintenanceService service, AsyncExceptionHandler exceptionHandler) {
        this.service = service;
        this.exceptionHandler = exceptionHandler;
    }

    @Scheduled(cron = "${task.cleanup.cron:0 0 2 * * *}", zone = "${sdk.timezone:UTC}")
    public void cleanup() {
        String traceId = UUID.randomUUID().toString();
        try {
            StructuredLog.withMdc(Map.of(
                    LogFields.TRACE_ID, traceId,
                    LogFields.EVENT_DATASET, "boilerplate.scheduler"), () -> {
                int deleted = service.removeExpiredCompletedTasks();
                StructuredLog.info(LOG, "Task cleanup completed", Map.of(
                        LogFields.EVENT_ACTION, "cleanupTasks",
                        LogFields.EVENT_OUTCOME, LogFields.OUTCOME_SUCCESS,
                        LogFields.EVENT_DATASET, "boilerplate.scheduler",
                        "task.deleted.count", deleted));
            });
        } catch (RuntimeException exception) {
            exceptionHandler.handle(traceId, "boilerplate.scheduler", "scheduler", "cleanupTasks",
                    Map.of(), exception);
        }
    }
}
