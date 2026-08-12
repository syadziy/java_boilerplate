package com.mac.boilerplate.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.TaskSearchCriteria;
import com.mac.boilerplate.entities.dto.TaskSearchResult;
import com.mac.boilerplate.entities.model.TaskSearchRow;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class JdbcTaskSearchRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-08-09T00:00:00Z");

    @Test
    @SuppressWarnings("unchecked")
    void searchesAndBuildsWindowedResults() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcTaskSearchRepository repository = new JdbcTaskSearchRepository(jdbc);
        UUID id = UUID.randomUUID();
        ResultSet rs = mock(ResultSet.class);
        when(rs.getObject("id", UUID.class)).thenReturn(id);
        when(rs.getString("title")).thenReturn("A");
        when(rs.getString("description")).thenReturn("B");
        when(rs.getString("status")).thenReturn("PENDING");
        when(rs.getTimestamp("created_at")).thenReturn(Timestamp.from(NOW));
        when(rs.getTimestamp("completed_at")).thenReturn(null);
        when(rs.getLong("total_count")).thenReturn(1L);
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenAnswer(invocation -> {
            RowMapper<TaskSearchRow> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(rs, 0));
        });

        TaskSearchResult result = repository.search(new TaskSearchCriteria(
                "A", TaskStatus.PENDING, NOW.minusSeconds(60), NOW.plusSeconds(60), 10, 0));

        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().id()).isEqualTo(id);
    }

    @Test
    @SuppressWarnings("unchecked")
    void returnsRowsWithinWindow() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcTaskSearchRepository repository = new JdbcTaskSearchRepository(jdbc);
        UUID id = UUID.randomUUID();
        ResultSet rs = mock(ResultSet.class);
        when(rs.getObject("id", UUID.class)).thenReturn(id);
        when(rs.getString("title")).thenReturn("A");
        when(rs.getString("description")).thenReturn("B");
        when(rs.getString("status")).thenReturn("COMPLETED");
        when(rs.getTimestamp("created_at")).thenReturn(Timestamp.from(NOW));
        when(rs.getTimestamp("completed_at")).thenReturn(Timestamp.from(NOW));
        when(rs.getLong("total_count")).thenReturn(0L);
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenAnswer(invocation -> {
            RowMapper<TaskSearchRow> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(rs, 0));
        });

        assertThat(repository.findWithin(NOW.minusSeconds(60), NOW.plusSeconds(60))).hasSize(1);
    }
}
