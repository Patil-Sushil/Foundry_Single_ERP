package com.kalibyte.foundry.labors.attendance.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.labors.attendance.dto.AttendanceRequestDTO;
import com.kalibyte.foundry.labors.attendance.dto.AttendanceResponseDTO;
import com.kalibyte.foundry.labors.attendance.dto.BulkAttendanceRequestDTO;
import com.kalibyte.foundry.labors.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse<AttendanceResponseDTO>> logAttendance(@RequestBody AttendanceRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Attendance logged successfully", attendanceService.logAttendance(request)));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Log bulk attendance", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDTO>>> bulkLogAttendance(@RequestBody BulkAttendanceRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Bulk attendance logged successfully", attendanceService.bulkLogAttendance(request)));
    }
}
