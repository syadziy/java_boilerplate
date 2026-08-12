package com.mac.boilerplate.repository;

import com.mac.boilerplate.entities.model.TaskAuditDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaskAuditRepository extends MongoRepository<TaskAuditDocument, String> {

    List<TaskAuditDocument> findByTaskIdOrderByOccurredAtDesc(UUID taskId);
}
