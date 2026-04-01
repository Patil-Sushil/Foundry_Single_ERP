package com.kalibyte.foundry.reports.gst.entity;

import com.kalibyte.foundry.reports.gst.entity.enums.ExportFormat;
import com.kalibyte.foundry.reports.gst.entity.enums.GstPeriodType;
import com.kalibyte.foundry.reports.gst.entity.enums.GstReportType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gst_report_audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GstReportAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private GstReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private GstPeriodType periodType;

    @Column(name = "period_from", nullable = false)
    private LocalDate periodFrom;

    @Column(name = "period_to", nullable = false)
    private LocalDate periodTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "export_format")
    private ExportFormat exportFormat;

    @Column(name = "ip_address")
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}