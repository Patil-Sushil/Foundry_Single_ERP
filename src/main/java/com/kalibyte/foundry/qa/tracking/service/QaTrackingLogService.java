package com.kalibyte.foundry.qa.tracking.service;

import com.kalibyte.foundry.qa.common.enums.TrackingAction;
import com.kalibyte.foundry.qa.common.enums.TrackingReferenceType;
import com.kalibyte.foundry.qa.tracking.entity.QaTrackingLog;

import java.time.LocalDateTime;
import java.util.List;

public interface QaTrackingLogService {
    void log(TrackingReferenceType type, Long id, String fromStatus, String toStatus, TrackingAction action, String performedBy, String remarks);
    List<QaTrackingLog> getLogs(TrackingReferenceType type, Long id);
    List<QaTrackingLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end);
}
