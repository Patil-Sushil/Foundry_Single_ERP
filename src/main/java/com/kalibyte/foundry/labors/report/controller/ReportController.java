package com.kalibyte.foundry.labors.report.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.labors.report.dto.LaborDetailedReport;
import com.kalibyte.foundry.labors.report.dto.LaborExpenseReport;
import com.kalibyte.foundry.labors.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/labor-reports")
@RequiredArgsConstructor
@Tag(name = "Labor Reports", description = "APIs for labor expense reporting and analytics")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/weekly")
    @Operation(summary = "Get weekly labor expense report", description = "Only accessible by ADMIN")
    public ResponseEntity<ApiResponse<LaborExpenseReport>> getWeeklyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getWeeklyReport(date)));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly labor expense report", description = "Only accessible by ADMIN")
    public ResponseEntity<ApiResponse<LaborExpenseReport>> getMonthlyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getMonthlyReport(date)));
    }

    @GetMapping("/yearly/{year}")
    @Operation(summary = "Get yearly labor expense report", description = "Only accessible by ADMIN")
    public ResponseEntity<ApiResponse<LaborExpenseReport>> getYearlyReport(@PathVariable int year) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getYearlyReport(year)));
    }

    @GetMapping("/export")
    @Operation(summary = "Export labor expense reports to Excel", description = "Only accessible by ADMIN")
    public ResponseEntity<byte[]> exportReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        List<LaborDetailedReport> reports = reportService.getDetailedReport(startDate, endDate);
        
        byte[] excelContent = reportService.exportToExcel(reports);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=labor_expense_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelContent);
    }
}

