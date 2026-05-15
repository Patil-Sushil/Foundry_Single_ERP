package com.kalibyte.foundry.qa.tracking.service.impl;

import com.kalibyte.foundry.qa.common.enums.TrackingAction;
import com.kalibyte.foundry.qa.common.enums.TrackingReferenceType;
import com.kalibyte.foundry.qa.tracking.entity.QaTrackingLog;
import com.kalibyte.foundry.qa.tracking.repository.QaTrackingLogRepository;
import com.kalibyte.foundry.qa.tracking.service.QaTrackingLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QaTrackingLogServiceImpl implements QaTrackingLogService {

    private final QaTrackingLogRepository repository;

    @Override
    @Transactional
    public void log(TrackingReferenceType type, Long id, String fromStatus, String toStatus, TrackingAction action, String performedBy, String remarks) {
        QaTrackingLog log = QaTrackingLog.builder()
                .referenceType(type)
                .referenceId(id)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .action(action)
                .performedBy(performedBy)
                .remarks(remarks)
                .build();
        repository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QaTrackingLog> getLogs(TrackingReferenceType type, Long id) {
        return repository.findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(type, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QaTrackingLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return repository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }
}
