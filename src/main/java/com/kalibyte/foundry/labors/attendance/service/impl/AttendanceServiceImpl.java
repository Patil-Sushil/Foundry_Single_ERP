package com.kalibyte.foundry.labors.attendance.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.labors.attendance.dto.AttendanceRequest;
import com.kalibyte.foundry.labors.attendance.dto.AttendanceResponse;
import com.kalibyte.foundry.labors.attendance.dto.BulkAttendanceRequest;
import com.kalibyte.foundry.labors.attendance.entity.Attendance;
import com.kalibyte.foundry.labors.attendance.exceptions.DuplicateAttendance;
import com.kalibyte.foundry.labors.attendance.mapper.AttendanceMapper;
import com.kalibyte.foundry.labors.attendance.repository.AttendanceRepository;
import com.kalibyte.foundry.labors.attendance.service.AttendanceService;
import com.kalibyte.foundry.labors.labor.entity.Enum.WageType;
import com.kalibyte.foundry.labors.labor.entity.Laborer;
import com.kalibyte.foundry.labors.labor.repository.LaborerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final LaborerRepository laborerRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    @Transactional
    public AttendanceResponse logAttendance(AttendanceRequest request) {
        Laborer laborer = laborerRepository.findById(request.getLaborerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Laborer not found with id: " + request.getLaborerId()));
        attendanceRepository
                .findByLaborerIdAndWorkDate(laborer.getId(), request.getWorkDate())
                .ifPresent(a -> {
                    throw new DuplicateAttendance("Attendance already exists");
                });
        BigDecimal earnedAmount = BigDecimal.ZERO;
        BigDecimal hoursWorked = BigDecimal.ZERO;
        BigDecimal appliedRate;
        WageType wageTypeSnapshot = laborer.getWageType();

        switch (laborer.getWageType()) {

            case DAILY:
                appliedRate = laborer.getDailyWage();
                earnedAmount = appliedRate;

                if (request.getCheckInTime() != null && request.getCheckOutTime() != null) {
                    Duration dailyDuration = Duration.between(
                            request.getCheckInTime(), request.getCheckOutTime());
                    hoursWorked = BigDecimal.valueOf(dailyDuration.toMinutes())
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                }
                break;

            case HOURLY:
                appliedRate = laborer.getHourlyRate();

                if (appliedRate == null) {
                    throw new IllegalStateException(
                            "Hourly rate not set for HOURLY laborer id: " + laborer.getId());
                }

                if (request.getCheckInTime() != null && request.getCheckOutTime() != null) {
                    Duration hourlyDuration = Duration.between(
                            request.getCheckInTime(), request.getCheckOutTime());
                    hoursWorked = BigDecimal.valueOf(hourlyDuration.toMinutes())
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

                    earnedAmount = hoursWorked.multiply(appliedRate)
                            .setScale(2, RoundingMode.HALF_UP);
                }
                break;

            case PIECE_RATE:
                appliedRate = laborer.getPieceRate();

                if (appliedRate == null) {
                    throw new IllegalStateException(
                            "Piece rate not set for PIECE_RATE laborer id: " + laborer.getId());
                }

                if (request.getPiecesCompleted() != null) {
                    earnedAmount = BigDecimal.valueOf(request.getPiecesCompleted())
                            .multiply(appliedRate)
                            .setScale(2, RoundingMode.HALF_UP);
                }
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported wage type: " + laborer.getWageType());
        }

        Attendance attendance = attendanceRepository
                .findByLaborerIdAndWorkDate(request.getLaborerId(), request.getWorkDate())
                .orElse(new Attendance());

        attendance.setLaborer(laborer);
        attendance.setWorkDate(request.getWorkDate());
        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setCheckOutTime(request.getCheckOutTime());
        attendance.setHoursWorked(hoursWorked);
        attendance.setPiecesCompleted(request.getPiecesCompleted());
        attendance.setEarnedAmount(earnedAmount);

        // ── Snapshot fields for audit ──
        attendance.setWageTypeSnapshot(wageTypeSnapshot);
        attendance.setAppliedRate(appliedRate);

        attendance = attendanceRepository.save(attendance);
        return attendanceMapper.toResponse(attendance);
    }

    @Override
    @Transactional
    public List<AttendanceResponse> bulkLogAttendance(BulkAttendanceRequest request) {
        return request.getLogs().stream()
                .map(this::logAttendance)
                .collect(Collectors.toList());
    }
}
