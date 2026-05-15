package com.kalibyte.foundry.labors.attendance.service;

import com.kalibyte.foundry.labors.attendance.dto.AttendanceRequest;
import com.kalibyte.foundry.labors.attendance.dto.AttendanceResponse;
import com.kalibyte.foundry.labors.attendance.dto.BulkAttendanceRequest;

import java.util.List;

public interface AttendanceService {
    AttendanceResponse logAttendance(AttendanceRequest request);
    List<AttendanceResponse> bulkLogAttendance(BulkAttendanceRequest request);
}
