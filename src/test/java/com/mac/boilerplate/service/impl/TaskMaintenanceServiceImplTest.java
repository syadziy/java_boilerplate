package com.mac.boilerplate.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.mac.boilerplate.config.properties.TaskProperties;
import com.mac.boilerplate.repository.TaskCacheRepository;
import com.mac.boilerplate.repository.TaskRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TaskMaintenanceServiceImplTest {

    @Test
    void clearsCacheOnlyWhenRowsWereDeleted() {
        TaskRepository repository = mock(TaskRepository.class);
        TaskCacheRepository cache = mock(TaskCacheRepository.class);
        Instant now = Instant.parse("2026-08-09T00:00:00Z");
        var properties = new TaskProperties(new TaskProperties.Cache(Duration.ofMinutes(10)),
                new TaskProperties.Cleanup(Duration.ofDays(30)));
        var service = new TaskMaintenanceServiceImpl(
                repository, cache, properties, Clock.fixed(now, ZoneOffset.UTC));
        Instant cutoff = now.minus(Duration.ofDays(30));

        when(repository.deleteCompletedBefore(cutoff)).thenReturn(0, 2);
        assertThat(service.removeExpiredCompletedTasks()).isZero();
        verifyNoInteractions(cache);
        assertThat(service.removeExpiredCompletedTasks()).isEqualTo(2);
        verify(cache).clear();
    }
}
