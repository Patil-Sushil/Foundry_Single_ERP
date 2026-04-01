package com.kalibyte.foundry.qa.tracking.repository;

import com.kalibyte.foundry.qa.common.enums.TrackingReferenceType;
import com.kalibyte.foundry.qa.tracking.entity.QaTrackingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QaTrackingLogRepository extends JpaRepository<QaTrackingLog, Long> {
    List<QaTrackingLog> findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(TrackingReferenceType referenceType, Long referenceId);

    List<QaTrackingLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
}
