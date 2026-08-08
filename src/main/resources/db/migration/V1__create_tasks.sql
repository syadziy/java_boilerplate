CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'COMPLETED')),
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ
);

CREATE INDEX idx_tasks_completed_cleanup
    ON tasks (completed_at)
    WHERE status = 'COMPLETED';
