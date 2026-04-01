package com.kalibyte.foundry.reports.gst.service;

import com.kalibyte.foundry.reports.gst.entity.enums.ExportFormat;
import com.kalibyte.foundry.reports.gst.entity.enums.GstPeriodType;
import com.kalibyte.foundry.reports.gst.entity.enums.GstReportType;

import java.time.LocalDate;
import java.util.UUID;

public interface GstReportAuditService {

    void logReportAccess(
            UUID userId,
            GstReportType reportType,
            GstPeriodType periodType,
            LocalDate from,
            LocalDate to,
            ExportFormat format,
            String ipAddress
    );
}