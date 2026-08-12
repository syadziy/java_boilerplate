CREATE INDEX IF NOT EXISTS idx_tasks_created_at ON tasks (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_tasks_status_created_at ON tasks (status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_tasks_completed_at ON tasks (completed_at DESC);
