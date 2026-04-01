package com.kalibyte.foundry.reports.gst.service.impl;

import com.kalibyte.foundry.reports.gst.entity.GstReportAuditLog;
import com.kalibyte.foundry.reports.gst.entity.enums.ExportFormat;
import com.kalibyte.foundry.reports.gst.entity.enums.GstPeriodType;
import com.kalibyte.foundry.reports.gst.entity.enums.GstReportType;
import com.kalibyte.foundry.reports.gst.repository.GstReportAuditLogRepository;
import com.kalibyte.foundry.reports.gst.service.GstReportAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GstReportAuditServiceImpl implements GstReportAuditService {

    private final GstReportAuditLogRepository auditLogRepository;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logReportAccess(
            UUID userId,
            GstReportType reportType,
            GstPeriodType periodType,
            LocalDate from,
            LocalDate to,
            ExportFormat format,
            String ipAddress) {

        try {
            GstReportAuditLog auditLog = GstReportAuditLog.builder()
                    .userId(userId)
                    .reportType(reportType)
                    .periodType(periodType)
                    .periodFrom(from)
                    .periodTo(to)
                    .exportFormat(format)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("GST report audit logged: {} by user {}", reportType, userId);

        } catch (Exception e) {
            // Audit failure should not break the report flow
            log.error("Failed to log GST report audit: {}", e.getMessage());
        }
    }
}