package com.kalibyte.foundry.labors.attendance.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequest {
    private Long laborerId;
    private LocalDate workDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private Integer piecesCompleted;
}
