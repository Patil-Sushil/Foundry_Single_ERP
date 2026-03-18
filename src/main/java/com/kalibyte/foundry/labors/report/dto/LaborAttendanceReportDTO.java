package com.kalibyte.foundry.labors.report.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaborAttendanceReportDTO {
    private LocalDate workDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private BigDecimal hoursWorked;
    private Integer pieceCompleted;
    private BigDecimal earnedAmount;
}
