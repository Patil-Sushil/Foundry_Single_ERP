package com.kalibyte.foundry.labors.attendance.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.labors.attendance.dto.AttendanceRequest;
import com.kalibyte.foundry.labors.attendance.dto.AttendanceResponse;
import com.kalibyte.foundry.labors.attendance.dto.BulkAttendanceRequest;
import com.kalibyte.foundry.labors.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Labor Attendance", description = "APIs for tracking daily labor attendance")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @Operation(summary = "Log single attendance", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> logAttendance(@RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Attendance logged successfully", attendanceService.logAttendance(request)));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Log bulk attendance", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> bulkLogAttendance(@RequestBody BulkAttendanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bulk attendance logged successfully", attendanceService.bulkLogAttendance(request)));
    }
}
