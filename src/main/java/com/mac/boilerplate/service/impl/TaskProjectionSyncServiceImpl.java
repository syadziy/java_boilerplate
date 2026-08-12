package com.mac.boilerplate.service.impl;

import com.mac.boilerplate.entities.mapper.TaskProjectionMapper;
import com.mac.boilerplate.repository.TaskAuditRepository;
import com.mac.boilerplate.repository.TaskSearchIndexRepository;
import com.mac.boilerplate.service.TaskProjectionSyncService;
import com.mac.boilerplate.entities.dto.TaskResponse;
import com.mac.sdk_util.entities.constant.LogFields;
import com.mac.sdk_util.utils.StructuredLog;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TaskProjectionSyncServiceImpl implements TaskProjectionSyncService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskProjectionSyncServiceImpl.class);

    private final TaskSearchIndexRepository searchIndexRepository;
    private final TaskAuditRepository auditRepository;
    private final TaskProjectionMapper mapper;

    public TaskProjectionSyncServiceImpl(
            TaskSearchIndexRepository searchIndexRepository,
            TaskAuditRepository auditRepository,
            TaskProjectionMapper mapper) {
        this.searchIndexRepository = searchIndexRepository;
        this.auditRepository = auditRepository;
        this.mapper = mapper;
    }

    @Override
    public void syncTask(TaskResponse response, String eventType) {
        try {
            searchIndexRepository.save(mapper.toSearchDocument(response));
            auditRepository.save(mapper.toAuditDocument(response, eventType));
            StructuredLog.info(LOG, "Task projections synced", Map.of(
                    LogFields.EVENT_ACTION, "syncTaskProjection",
                    LogFields.EVENT_OUTCOME, LogFields.OUTCOME_SUCCESS,
                    LogFields.EVENT_DATASET, "boilerplate.projection",
                    "task.id", response.id(),
                    "task.event.type", eventType));
        } catch (RuntimeException exception) {
            StructuredLog.warn(LOG, "Task projections sync failed", new LinkedHashMap<>(Map.of(
                    LogFields.EVENT_ACTION, "syncTaskProjection",
                    LogFields.EVENT_OUTCOME, LogFields.OUTCOME_FAILURE,
                    LogFields.EVENT_DATASET, "boilerplate.projection",
                    "task.id", response.id(),
                    "task.event.type", eventType,
                    "error.type", exception.getClass().getSimpleName())));
        }
    }
}
