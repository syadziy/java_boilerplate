package com.mac.boilerplate.repository.impl;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.model.Task;
import com.mac.boilerplate.repository.TaskRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTaskRepository implements TaskRepository {

    private static final String INSERT = """
            INSERT INTO tasks (id, title, description, status, created_at, completed_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String FIND_BY_ID = """
            SELECT id, title, description, status, created_at, completed_at
            FROM tasks WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcTaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Task save(Task task) {
        jdbcTemplate.update(INSERT, task.id(), task.title(), task.description(), task.status().name(),
                Timestamp.from(task.createdAt()), task.completedAt());
        return task;
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return jdbcTemplate.query(FIND_BY_ID, this::map, id).stream().findFirst();
    }

    @Override
    public boolean markCompleted(UUID id, Instant completedAt) {
        return jdbcTemplate.update("""
                UPDATE tasks SET status = 'COMPLETED', completed_at = ?
                WHERE id = ? AND status = 'PENDING'
                """, Timestamp.from(completedAt), id) == 1;
    }

    @Override
    public int deleteCompletedBefore(Instant cutoff) {
        return jdbcTemplate.update(
                "DELETE FROM tasks WHERE status = 'COMPLETED' AND completed_at < ?", Timestamp.from(cutoff));
    }

    private Task map(ResultSet rs, int rowNumber) throws SQLException {
        Timestamp completedAt = rs.getTimestamp("completed_at");
        return new Task(
                rs.getObject("id", UUID.class),
                rs.getString("title"),
                rs.getString("description"),
                TaskStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toInstant(),
                completedAt == null ? null : completedAt.toInstant());
    }
}
