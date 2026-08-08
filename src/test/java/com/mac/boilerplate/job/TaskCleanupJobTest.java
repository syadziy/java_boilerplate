package com.mac.boilerplate.job;

import static org.mockito.Mockito.*;

import com.mac.boilerplate.service.TaskMaintenanceService;
import com.mac.boilerplate.utils.handler.AsyncExceptionHandler;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TaskCleanupJobTest {

    @Test
    void delegatesCleanupAndHandlesBoundaryFailure() {
        TaskMaintenanceService service = mock(TaskMaintenanceService.class);
        AsyncExceptionHandler handler = mock(AsyncExceptionHandler.class);
        TaskCleanupJob job = new TaskCleanupJob(service, handler);

        when(service.removeExpiredCompletedTasks()).thenReturn(2).thenThrow(new IllegalStateException("db"));
        job.cleanup();
        job.cleanup();

        verify(service, times(2)).removeExpiredCompletedTasks();
        verify(handler).handle(anyString(), eq("boilerplate.scheduler"), eq("scheduler"),
                eq("cleanupTasks"), eq(Map.of()), any(IllegalStateException.class));
    }
}
