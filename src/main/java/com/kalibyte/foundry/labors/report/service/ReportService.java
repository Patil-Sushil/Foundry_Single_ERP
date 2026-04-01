package com.kalibyte.foundry.labors.report.service;

import com.kalibyte.foundry.labors.attendance.entity.Attendance;
import com.kalibyte.foundry.labors.attendance.repository.AttendanceRepository;
import com.kalibyte.foundry.labors.report.dto.LaborAttendanceReportDTO;
import com.kalibyte.foundry.labors.report.dto.LaborDetailedReportDTO;
import com.kalibyte.foundry.labors.report.dto.LaborExpenseReportDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AttendanceRepository attendanceRepository;

    public List<LaborDetailedReportDTO> getDetailedReport(LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = attendanceRepository.findByWorkDateBetween(startDate, endDate);

        return attendances.stream()
                .collect(Collectors.groupingBy(a -> a.getLaborer().getId()))
                .values().stream()
                .map(laborerAttendances -> {
                    String laborerName = laborerAttendances.getFirst().getLaborer().getName();

                    BigDecimal totalHours = laborerAttendances.stream()
                            .map(a -> a.getHoursWorked() != null ? a.getHoursWorked() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalEarned = laborerAttendances.stream()
                            .map(Attendance::getEarnedAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    List<LaborAttendanceReportDTO> attendanceDetails = laborerAttendances.stream()
                            .sorted(Comparator.comparing(Attendance::getWorkDate))
                            .map(a -> LaborAttendanceReportDTO.builder()
                                    .workDate(a.getWorkDate())
                                    .checkInTime(a.getCheckInTime())
                                    .checkOutTime(a.getCheckOutTime())
                                    .pieceCompleted(a.getPiecesCompleted())
                                    .hoursWorked(a.getHoursWorked())
                                    .earnedAmount(a.getEarnedAmount())
                                    .build())
                            .collect(Collectors.toList());

                    return LaborDetailedReportDTO.builder()
                            .laborerName(laborerName)
                            .totalHours(totalHours)
                            .totalEarned(totalEarned)
                            .attendanceDetails(attendanceDetails)
                            .build();
                })
                .sorted(Comparator.comparing(LaborDetailedReportDTO::getLaborerName))
                .collect(Collectors.toList());
    }

    public LaborExpenseReportDTO getAggregatedExpensesForPeriod(LocalDate startDate, LocalDate endDate, String periodLabel) {
        List<LaborDetailedReportDTO> laborDetails = getDetailedReport(startDate, endDate);

        BigDecimal totalHours = laborDetails.stream()
                .map(LaborDetailedReportDTO::getTotalHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLaborCost = laborDetails.stream()
                .map(LaborDetailedReportDTO::getTotalEarned)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalWorkers = laborDetails.size();

        return LaborExpenseReportDTO.builder()
                .period(periodLabel)
                .totalHours(totalHours)
                .totalLaborCost(totalLaborCost)
                .totalWorkers(totalWorkers)
                .laborDetails(laborDetails)
                .build();
    }

    public LaborExpenseReportDTO getWeeklyReport(LocalDate date) {
        LocalDate start = date.with(java.time.DayOfWeek.MONDAY);
        LocalDate end = date.with(java.time.DayOfWeek.SUNDAY);
        int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        String label = date.getYear() + "-W" + week;
        return getAggregatedExpensesForPeriod(start, end, label);
    }

    public LaborExpenseReportDTO getMonthlyReport(LocalDate date) {
        LocalDate start = date.withDayOfMonth(1);
        LocalDate end = date.withDayOfMonth(date.lengthOfMonth());
        String label = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return getAggregatedExpensesForPeriod(start, end, label);
    }

    public LaborExpenseReportDTO getYearlyReport(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        return getAggregatedExpensesForPeriod(start, end, String.valueOf(year));
    }

    public byte[] exportToExcel(List<LaborDetailedReportDTO> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Labor Detailed Report");

            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Labor Name", "Date", "Check In", "Check Out", "Hours Worked", "Earned Amount"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Data Rows
            int rowIdx = 1;
            for (LaborDetailedReportDTO laborReport : reports) {
                for (LaborAttendanceReportDTO attendance : laborReport.getAttendanceDetails()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(laborReport.getLaborerName());
                    row.createCell(1).setCellValue(attendance.getWorkDate().toString());
                    row.createCell(2).setCellValue(attendance.getCheckInTime() != null ? attendance.getCheckInTime().toString() : "-");
                    row.createCell(3).setCellValue(attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().toString() : "-");
                    row.createCell(4).setCellValue(attendance.getHoursWorked() != null ? attendance.getHoursWorked().doubleValue() : 0.0);
                    row.createCell(5).setCellValue(attendance.getEarnedAmount().doubleValue());
                }
                
                // Summary row per labor
                Row summaryRow = sheet.createRow(rowIdx++);
                Cell labelCell = summaryRow.createCell(0);
                labelCell.setCellValue("TOTAL FOR " + laborReport.getLaborerName());
                labelCell.setCellStyle(boldStyle);
                
                Cell totalHoursCell = summaryRow.createCell(4);
                totalHoursCell.setCellValue(laborReport.getTotalHours().doubleValue());
                totalHoursCell.setCellStyle(boldStyle);
                
                Cell totalEarnedCell = summaryRow.createCell(5);
                totalEarnedCell.setCellValue(laborReport.getTotalEarned().doubleValue());
                totalEarnedCell.setCellStyle(boldStyle);
                
                rowIdx++; // Empty row for spacing
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
