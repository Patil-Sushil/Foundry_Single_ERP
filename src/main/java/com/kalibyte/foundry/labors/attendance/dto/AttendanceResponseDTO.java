package com.kalibyte.foundry.labors.attendance.dto;

import com.kalibyte.foundry.labors.labor.entity.Enum.WageType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponseDTO {
    private Long id;
    private Long laborerId;
    private String laborerName;
    private LocalDate workDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private BigDecimal hoursWorked;
    private Integer piecesCompleted;
    private BigDecimal earnedAmount;
    private WageType wageTypeSnapshot;
    private BigDecimal appliedRate;
}
