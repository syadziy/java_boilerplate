package com.mac.boilerplate.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.model.Task;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class JdbcTaskRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-08-09T00:00:00Z");

    @Test
    void savesUpdatesAndDeletes() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcTaskRepository repository = new JdbcTaskRepository(jdbc);
        Task task = new Task(UUID.randomUUID(), "A", null, TaskStatus.PENDING, NOW, null);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1, 1, 3);

        assertThat(repository.save(task)).isSameAs(task);
        assertThat(repository.markCompleted(task.id(), NOW)).isTrue();
        assertThat(repository.deleteCompletedBefore(NOW)).isEqualTo(3);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findsAndMapsRowsWithOptionalCompletion() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcTaskRepository repository = new JdbcTaskRepository(jdbc);
        UUID id = UUID.randomUUID();
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("id", UUID.class)).thenReturn(id);
        when(resultSet.getString("title")).thenReturn("A");
        when(resultSet.getString("description")).thenReturn("B");
        when(resultSet.getString("status")).thenReturn("COMPLETED");
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.from(NOW));
        when(resultSet.getTimestamp("completed_at")).thenReturn(Timestamp.from(NOW), null);
        when(jdbc.query(anyString(), any(RowMapper.class), eq(id))).thenAnswer(invocation -> {
            RowMapper<Task> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(resultSet, 0));
        });

        assertThat(repository.findById(id).orElseThrow().completedAt()).isEqualTo(NOW);
        assertThat(repository.findById(id).orElseThrow().completedAt()).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void returnsEmptyWhenNoRowAndFalseWhenNotUpdated() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcTaskRepository repository = new JdbcTaskRepository(jdbc);
        UUID id = UUID.randomUUID();
        when(jdbc.query(anyString(), any(RowMapper.class), eq(id))).thenReturn(List.of());
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(0);

        assertThat(repository.findById(id)).isEmpty();
        assertThat(repository.markCompleted(id, NOW)).isFalse();
    }
}
