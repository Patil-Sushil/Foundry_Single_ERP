package com.kalibyte.foundry.labors.report.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaborExpenseReport {
    private String period; // e.g., "2023-W42", "2023-10", "2023"
    private BigDecimal totalHours;
    private BigDecimal totalLaborCost;
    private Long totalWorkers;
    private List<LaborDetailedReport> laborDetails;
}
