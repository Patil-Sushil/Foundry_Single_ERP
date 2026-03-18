package com.kalibyte.foundry.labors.report.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaborDetailedReportDTO {
    private String laborerName;
    private BigDecimal totalHours;
    private BigDecimal totalEarned;
    private List<LaborAttendanceReportDTO> attendanceDetails;
}
