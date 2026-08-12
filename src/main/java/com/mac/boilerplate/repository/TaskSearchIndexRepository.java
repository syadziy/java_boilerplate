package com.mac.boilerplate.repository;

import com.mac.boilerplate.entities.constant.TaskStatus;
import com.mac.boilerplate.entities.model.TaskSearchDocument;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TaskSearchIndexRepository extends ElasticsearchRepository<TaskSearchDocument, String> {

    List<TaskSearchDocument> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description);

    List<TaskSearchDocument> findByStatus(TaskStatus status);
}
