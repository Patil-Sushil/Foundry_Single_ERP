package com.kalibyte.foundry.reports.gst.controller;

import com.kalibyte.foundry.auth.security.token.CustomUserDetails;
import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.reports.gst.dto.request.GstReportRequest;
import com.kalibyte.foundry.reports.gst.dto.response.b2b.Gstr1B2BReport;
import com.kalibyte.foundry.reports.gst.dto.response.b2c.Gstr1B2CReport;
import com.kalibyte.foundry.reports.gst.dto.response.document.DocumentSummaryReport;
import com.kalibyte.foundry.reports.gst.dto.response.hsn.HsnSummaryReport;
import com.kalibyte.foundry.reports.gst.dto.response.salesregister.SalesRegisterReport;
import com.kalibyte.foundry.reports.gst.dto.response.taxliability.TaxLiabilitySummary;
import com.kalibyte.foundry.reports.gst.entity.enums.ExportFormat;
import com.kalibyte.foundry.reports.gst.entity.enums.GstReportType;
import com.kalibyte.foundry.reports.gst.service.GstExcelExportService;
import com.kalibyte.foundry.reports.gst.service.GstOutwardReportService;
import com.kalibyte.foundry.reports.gst.service.GstReportAuditService;
import com.kalibyte.foundry.reports.gst.util.GstPeriodResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gst/outward")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('CA', 'ADMIN')")
public class GstOutwardReportController {

    private final GstOutwardReportService reportService;
    private final GstExcelExportService excelExportService;
    private final GstReportAuditService auditService;

    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    // ================================================
    // B2B REPORT
    // ================================================

    @PostMapping("/b2b")
    public ApiResponse<Gstr1B2BReport> getB2BReport(
            @Valid @RequestBody GstReportRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        Gstr1B2BReport report = reportService.generateB2BReport(request);
        logAudit(user, GstReportType.GSTR1_B2B, request, ExportFormat.JSON, httpRequest);
        return ApiResponse.success(report);
    }

    @PostMapping("/b2b/download")
    public ResponseEntity<byte[]> downloadB2B(
            @Valid @RequestBody GstReportRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        Gstr1B2BReport report = reportService.generateB2BReport(request);
        byte[] excel = excelExportService.exportB2BExcel(report);
        logAudit(user, GstReportType.GSTR1_B2B, request, ExportFormat.XLSX, httpRequest);

        String filename = "GSTR1_B2B_" + GstPeriodResolver.filenameSuffix(request) + ".xlsx";
        return buildExcelResponse(excel, filename);
    }

    // ================================================
    // B2C LARGE REPORT
    // ================================================

    @PostMapping("/b2c-large")
    public ApiResponse<Gstr1B2CReport> getB2CLargeReport(
            @Valid @RequestBody GstReportRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        Gstr1B2CReport report = reportService.generateB2CLargeReport(request);
        logAudit(user, GstReportType.GSTR1_B2C_LARGE, request, ExportFormat.JSON, httpRequest);
        return ApiResponse.success(report);
    }

    @PostMapping("/b2c-large/download")
    public ResponseEntity<byte[]> downloadB2CLarge(
            @Valid @RequestBody GstReportRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        Gstr1B2CReport report = reportService.generateB2CLargeReport(request);
        byte[] excel = excelExportService.exportB2CExcel(report);
        logAudit(user, GstReportType.GSTR1_B2C_LARGE, request, ExportFormat.XLSX, httpRequest);

        String filename = "GSTR1_B2C_LARGE_" + GstPeriodResolver.filenameSuffix(request) + ".xlsx";
        return buildExcelResponse(excel, filename);
    }

    // ================================================
    // B2C SMALL REPORT
    // ================================================

    @PostMapping("/b2c-small")
    public ApiResponse<Gstr1B2CReport> getB2CSmallReport(
            @Valid @RequestBody GstReportRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        Gstr1B2CReport report = reportService.generateB2CSmallReport(request);
        logAudit(user, GstReportType.GSTR1_B2C_SMALL, request, ExportFormat.JSON, httpRequest);
        return ApiResponse.success(report);
    }

    @PostMapping("/b2c-small/download")
    public ResponseEntity<byte[]> downloadB2CSmall(
            @Valid @RequestBody GstReportRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        Gstr1B2CReport report = reportService.generateB2CSmallReport(request);
        byte[] excel = excelExportService.exportB2CExcel(report);
        logAudit(user, GstReportType.GSTR1_B2C_SMALL, request, ExportFormat.XLSX, httpRequest);

        String filename = "GSTR1_B2C_SMALL_" + GstPeriodResolver.filenameSuffix(request) + ".xlsx";
        return buildExcelResponse(excel, filename);
    }

    // ================================================
    // HSN SUMMARY
    // ================================================

    @PostMapping("/hsn-summary")
    public ApiResponse<HsnSummaryReport> getHsnSummary(
            @Valid @RequestBody GstReportRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        HsnSummaryReport report = reportService.generateHsnSummary(request);
        logAudit(user, GstReportType.GSTR1_HSN_SUMMARY, request, ExportFormat.JSON, httpRequest);
        return ApiResponse.success(report);
    }

    @PostMapping("/hsn-summary/download")
    public ResponseEntity<byte[]> downloadHsnSummary(
            @Valid @RequestBody GstReportRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        HsnSummaryReport report = reportService.generateHsnSummary(request);
        byte[] excel = excelExportService.exportHsnSummaryExcel(report);
        logAudit(user, GstReportType.GSTR1_HSN_SUMMARY, request, ExportFormat.XLSX, httpRequest);

        String filename = "GSTR1_HSN_" + GstPeriodResolver.filenameSuffix(request) + ".xlsx";
        return buildExcelResponse(excel, filename);
    }

    // ================================================
    // DOCUMENT SUMMARY
    // ================================================

    @PostMapping("/document-summary")
    public ApiResponse<DocumentSummaryReport> getDocumentSummary(
            @Valid @RequestBody GstReportRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        DocumentSummaryReport report = reportService.generateDocumentSummary(request);
        logAudit(user, GstReportType.GSTR1_DOCUMENT_SUMMARY, request, ExportFormat.JSON, httpRequest);
        return ApiResponse.success(report);
    }

    // ================================================
    // SALES REGISTER
    // ================================================

    @PostMapping("/sales-register")
    public ApiResponse<SalesRegisterReport> getSalesRegister(
            @Valid @RequestBody GstReportRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        SalesRegisterReport report = reportService.generateSalesRegister(request);
        logAudit(user, GstReportType.SALES_REGISTER, request, ExportFormat.JSON, httpRequest);
        return ApiResponse.success(report);
    }

    @PostMapping("/sales-register/download")
    public ResponseEntity<byte[]> downloadSalesRegister(
            @Valid @RequestBody GstReportRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        SalesRegisterReport report = reportService.generateSalesRegister(request);
        byte[] excel = excelExportService.exportSalesRegisterExcel(report);
        logAudit(user, GstReportType.SALES_REGISTER, request, ExportFormat.XLSX, httpRequest);

        String filename = "Sales_Register_" + GstPeriodResolver.filenameSuffix(request) + ".xlsx";
        return buildExcelResponse(excel, filename);
    }

    // ================================================
    // TAX LIABILITY SUMMARY
    // ================================================

    @PostMapping("/tax-liability")
    public ApiResponse<TaxLiabilitySummary> getTaxLiability(
            @Valid @RequestBody GstReportRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        TaxLiabilitySummary report = reportService.generateTaxLiabilitySummary(request);
        logAudit(user, GstReportType.TAX_LIABILITY_SUMMARY, request, ExportFormat.JSON, httpRequest);
        return ApiResponse.success(report);
    }

    @PostMapping("/tax-liability/download")
    public ResponseEntity<byte[]> downloadTaxLiability(
            @Valid @RequestBody GstReportRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        TaxLiabilitySummary report = reportService.generateTaxLiabilitySummary(request);
        byte[] excel = excelExportService.exportTaxLiabilityExcel(report);
        logAudit(user, GstReportType.TAX_LIABILITY_SUMMARY, request, ExportFormat.XLSX, httpRequest);

        String filename = "Tax_Liability_" + GstPeriodResolver.filenameSuffix(request) + ".xlsx";
        return buildExcelResponse(excel, filename);
    }

    // ================================================
    // HELPERS
    // ================================================

    private ResponseEntity<byte[]> buildExcelResponse(byte[] data, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .contentType(XLSX_MEDIA_TYPE)
                .contentLength(data.length)
                .body(data);
    }

    private void logAudit(CustomUserDetails user, GstReportType type,
                          GstReportRequest request, ExportFormat format,
                          HttpServletRequest httpRequest) {
        auditService.logReportAccess(
                user.getId(), type, request.getPeriodType(),
                request.resolvedFromDate(), request.resolvedToDate(),
                format, httpRequest.getRemoteAddr()
        );
    }
}