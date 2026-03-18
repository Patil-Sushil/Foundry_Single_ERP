package com.kalibyte.foundry.labors.attendance.service;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.labors.attendance.dto.AttendanceRequestDTO;
import com.kalibyte.foundry.labors.attendance.dto.AttendanceResponseDTO;
import com.kalibyte.foundry.labors.attendance.dto.BulkAttendanceRequestDTO;
import com.kalibyte.foundry.labors.attendance.entity.Attendance;
import com.kalibyte.foundry.labors.attendance.exceptions.DuplicateAttendance;
import com.kalibyte.foundry.labors.attendance.repository.AttendanceRepository;
import com.kalibyte.foundry.labors.labor.entity.Enum.WageType;
import com.kalibyte.foundry.labors.labor.entity.Laborer;
import com.kalibyte.foundry.labors.labor.repository.LaborerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final LaborerRepository laborerRepository;

	public AttendanceService(AttendanceRepository attendanceRepository, LaborerRepository laborerRepository) {
		this.attendanceRepository = attendanceRepository;
		this.laborerRepository = laborerRepository;
	}

	@Transactional
    public AttendanceResponseDTO logAttendance(AttendanceRequestDTO request) {
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
        return mapToResponse(attendance);
    }

    @Transactional
    public List<AttendanceResponseDTO> bulkLogAttendance(BulkAttendanceRequestDTO request) {
        return request.getLogs().stream()
                .map(this::logAttendance)
                .collect(Collectors.toList());
    }

    private AttendanceResponseDTO mapToResponse(Attendance attendance) {
        return AttendanceResponseDTO.builder()
                .id(attendance.getId())
                .laborerId(attendance.getLaborer().getId())
                .laborerName(attendance.getLaborer().getName())
                .workDate(attendance.getWorkDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .hoursWorked(attendance.getHoursWorked())
                .piecesCompleted(attendance.getPiecesCompleted())
                .earnedAmount(attendance.getEarnedAmount())
                .wageTypeSnapshot(attendance.getWageTypeSnapshot())
                .appliedRate(attendance.getAppliedRate())
                .build();
    }
}