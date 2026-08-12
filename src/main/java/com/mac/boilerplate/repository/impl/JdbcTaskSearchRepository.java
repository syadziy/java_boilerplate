package com.mac.boilerplate.repository.impl;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.dto.TaskSearchCriteria;
import com.mac.boilerplate.entities.dto.TaskSearchResult;
import com.mac.boilerplate.entities.model.TaskSearchRow;
import com.mac.boilerplate.repository.TaskSearchRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTaskSearchRepository implements TaskSearchRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTaskSearchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public TaskSearchResult search(TaskSearchCriteria criteria) {
        var query = normalize(criteria.query());
        var from = criteria.from();
        var to = criteria.to();
        List<Object> args = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                WITH filtered AS (
                    SELECT id, title, description, status, created_at, completed_at
                    FROM tasks
                    WHERE created_at >= ?
                      AND created_at < ?
                """);
        args.add(Timestamp.from(from));
        args.add(Timestamp.from(to));

        if (criteria.status() != null) {
            sql.append(" AND status = ?");
            args.add(criteria.status().name());
        }

        if (!query.isBlank()) {
            sql.append("""
                      AND (
                          LOWER(title) LIKE ?
                          OR LOWER(COALESCE(description, '')) LIKE ?
                      )
                    """);
            args.add("%" + query + "%");
            args.add("%" + query + "%");
        }

        sql.append("""
                )
                SELECT id, title, description, status, created_at, completed_at,
                       COUNT(*) OVER() AS total_count
                FROM filtered
                ORDER BY created_at DESC, id DESC
                LIMIT ? OFFSET ?
                """);
        args.add(criteria.limit());
        args.add(criteria.offset());

        List<TaskSearchRow> rows = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new TaskSearchRow(
                rs.getObject("id", UUID.class),
                rs.getString("title"),
                rs.getString("description"),
                TaskStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toInstant(),
                toInstant(rs.getTimestamp("completed_at")),
                rs.getLong("total_count")),
                args.toArray());

        long totalCount = rows.isEmpty() ? 0 : rows.getFirst().totalCount();
        return new TaskSearchResult(
                rows.stream().map(row -> new com.mac.boilerplate.entities.dto.TaskResponse(
                        row.id(), row.title(), row.description(), row.status(), row.createdAt(), row.completedAt()))
                        .toList(),
                totalCount,
                criteria.limit(),
                criteria.offset());
    }

    @Override
    public List<TaskSearchRow> findWithin(Instant from, Instant to) {
        return jdbcTemplate.query("""
                SELECT id, title, description, status, created_at, completed_at, 0 AS total_count
                FROM tasks
                WHERE created_at >= ? AND created_at < ?
                ORDER BY created_at DESC, id DESC
                """,
                (rs, rowNum) -> new TaskSearchRow(
                        rs.getObject("id", UUID.class),
                        rs.getString("title"),
                        rs.getString("description"),
                        TaskStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toInstant(),
                        toInstant(rs.getTimestamp("completed_at")),
                        rs.getLong("total_count")),
                Timestamp.from(from),
                Timestamp.from(to));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
