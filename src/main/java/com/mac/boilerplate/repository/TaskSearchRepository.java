package com.mac.boilerplate.repository;

import com.mac.boilerplate.entities.dto.TaskSearchCriteria;
import com.mac.boilerplate.entities.dto.TaskSearchResult;
import com.mac.boilerplate.entities.model.TaskSearchRow;
import java.time.Instant;
import java.util.List;

public interface TaskSearchRepository {

    TaskSearchResult search(TaskSearchCriteria criteria);

    List<TaskSearchRow> findWithin(Instant from, Instant to);
}
