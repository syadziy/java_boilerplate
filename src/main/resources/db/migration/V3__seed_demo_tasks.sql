INSERT INTO tasks (id, title, description, status, created_at, completed_at) VALUES
('11111111-1111-1111-1111-111111111111', 'Learn Redis cache-aside', 'Read from Redis before PostgreSQL', 'COMPLETED', '2026-08-01T02:00:00Z', '2026-08-01T02:45:00Z'),
('22222222-2222-2222-2222-222222222222', 'Build Kafka consumer', 'Consume task.create events and validate payloads', 'COMPLETED', '2026-08-02T03:00:00Z', '2026-08-02T03:20:00Z'),
('33333333-3333-3333-3333-333333333333', 'Design dashboard query', 'Use native SQL and window functions for reporting', 'PENDING', '2026-08-03T04:00:00Z', NULL),
('44444444-4444-4444-4444-444444444444', 'Index audit trail in Mongo', 'Persist task lifecycle events for traceability', 'PENDING', '2026-08-04T05:00:00Z', NULL),
('55555555-5555-5555-5555-555555555555', 'Expose search endpoint', 'Provide query and status filters for recruiters', 'COMPLETED', '2026-08-05T06:00:00Z', '2026-08-05T06:30:00Z'),
('66666666-6666-6666-6666-666666666666', 'Prepare demo flow', 'Show REST, Kafka, Redis, MongoDB, and Elasticsearch', 'PENDING', '2026-08-06T07:00:00Z', NULL);
