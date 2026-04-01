package com.kalibyte.foundry.reports.gst.repository;

import com.kalibyte.foundry.reports.gst.entity.GstReportAuditLog;
import com.kalibyte.foundry.reports.gst.entity.enums.GstReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface GstReportAuditLogRepository extends JpaRepository<GstReportAuditLog, UUID> {

    Page<GstReportAuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<GstReportAuditLog> findByReportTypeOrderByCreatedAtDesc(GstReportType reportType, Pageable pageable);

    long countByUserIdAndCreatedAtAfter(UUID userId, LocalDateTime after);
}