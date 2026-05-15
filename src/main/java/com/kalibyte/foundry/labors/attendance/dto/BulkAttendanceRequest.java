package com.kalibyte.foundry.labors.attendance.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAttendanceRequest {
    private List<AttendanceRequest> logs;
}
