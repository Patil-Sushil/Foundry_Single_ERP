package com.kalibyte.foundry.qa.tracking.service;

import com.kalibyte.foundry.qa.common.enums.TrackingAction;
import com.kalibyte.foundry.qa.common.enums.TrackingReferenceType;
import com.kalibyte.foundry.qa.tracking.entity.QaTrackingLog;
import com.kalibyte.foundry.qa.tracking.repository.QaTrackingLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QaTrackingLogService {

    private final QaTrackingLogRepository repository;

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

    @Transactional(readOnly = true)
    public List<QaTrackingLog> getLogs(TrackingReferenceType type, Long id) {
        return repository.findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(type, id);
    }

    @Transactional(readOnly = true)
    public List<QaTrackingLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return repository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }
}
