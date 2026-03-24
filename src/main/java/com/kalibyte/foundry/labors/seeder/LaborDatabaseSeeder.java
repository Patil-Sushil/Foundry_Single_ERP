package com.kalibyte.foundry.labors.seeder;

import com.kalibyte.foundry.labors.advance.dto.AdvanceTransactionRequestDTO;
import com.kalibyte.foundry.labors.advance.entity.Enum.TransactionType;
import com.kalibyte.foundry.labors.advance.service.AdvanceService;
import com.kalibyte.foundry.labors.attendance.dto.AttendanceRequestDTO;
import com.kalibyte.foundry.labors.attendance.service.AttendanceService;
import com.kalibyte.foundry.labors.labor.dto.LaborerRequestDTO;
import com.kalibyte.foundry.labors.labor.dto.LaborerResponseDTO;
import com.kalibyte.foundry.labors.labor.entity.Enum.WageType;
import com.kalibyte.foundry.labors.labor.repository.LaborerRepository;
import com.kalibyte.foundry.labors.labor.service.LaborerService;
import com.kalibyte.foundry.labors.payout.dto.WeeklyPayoutRequestDTO;
import com.kalibyte.foundry.labors.payout.service.WeeklyPayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("seed") // Use a specific profile for seeding inventory
@Order(3) // Run after AdminBootstrap and DataSeeder and InventorySeeder
public class LaborDatabaseSeeder implements CommandLineRunner {

    private final LaborerRepository laborerRepository;
    private final LaborerService laborerService;
    private final AttendanceService attendanceService;
    private final AdvanceService advanceService;
    private final WeeklyPayoutService weeklyPayoutService;

    @Override
    public void run(String... args) throws Exception {
        if (laborerRepository.count() == 0) {
            log.info("Seeding Labor Management data...");

            // 1. Create Laborers
            LaborerResponseDTO hourly1 = laborerService.createLaborer(LaborerRequestDTO.builder()
                    .name("John Doe")
                    .wageType(WageType.HOURLY)
                    .dailyWage(new BigDecimal("800.00")) // 100 per hour
                    .hourlyRate(new BigDecimal("80"))
                    .isActive(true)
                    .build());

            LaborerResponseDTO hourly2 = laborerService.createLaborer(LaborerRequestDTO.builder()
                    .name("Jane Smith")
                    .wageType(WageType.HOURLY)
                    .dailyWage(new BigDecimal("1000.00")) // 125 per hour
                    .hourlyRate(new BigDecimal("100"))
                    .isActive(true)
                    .build());

            LaborerResponseDTO pieceRate1 = laborerService.createLaborer(LaborerRequestDTO.builder()
                    .name("Bob Wilson")
                    .wageType(WageType.PIECE_RATE)
                    .pieceRate(new BigDecimal("50.00"))
                    .isActive(true)
                    .build());

            // 2. Log 5 days of attendance
            LocalDate today = LocalDate.now();
            for (int i = 1; i <= 5; i++) {
                LocalDate workDate = today.minusDays(i);

                // Hourly workers
                attendanceService.logAttendance(AttendanceRequestDTO.builder()
                        .laborerId(hourly1.getId())
                        .workDate(workDate)
                        .checkInTime(LocalTime.of(9, 0))
                        .checkOutTime(LocalTime.of(17, 0)) // 8 hours
                        .build());

                attendanceService.logAttendance(AttendanceRequestDTO.builder()
                        .laborerId(hourly2.getId())
                        .workDate(workDate)
                        .checkInTime(LocalTime.of(8, 0))
                        .checkOutTime(LocalTime.of(18, 0)) // 10 hours
                        .build());

                // Piece rate worker
                attendanceService.logAttendance(AttendanceRequestDTO.builder()
                        .laborerId(pieceRate1.getId())
                        .workDate(workDate)
                        .piecesCompleted(20) // 20 * 50 = 1000
                        .build());
            }

            // 3. Grant $500 advance to John Doe
            advanceService.grantAdvance(AdvanceTransactionRequestDTO.builder()
                    .laborerId(hourly1.getId())
                    .transactionDate(today.minusDays(6))
                    .amount(new BigDecimal("500.00"))
                    .transactionType(TransactionType.GIVEN)
                    .notes("Initial loan")
                    .build());

            // 4. Generate Weekly Payouts
            LocalDate weekStart = today.minusDays(7);
            LocalDate weekEnd = today;

            weeklyPayoutService.generateWeeklyPayout(WeeklyPayoutRequestDTO.builder()
                    .laborerId(hourly1.getId())
                    .weekStartDate(weekStart)
                    .weekEndDate(weekEnd)
                    .build());

            weeklyPayoutService.generateWeeklyPayout(WeeklyPayoutRequestDTO.builder()
                    .laborerId(hourly2.getId())
                    .weekStartDate(weekStart)
                    .weekEndDate(weekEnd)
                    .build());

            weeklyPayoutService.generateWeeklyPayout(WeeklyPayoutRequestDTO.builder()
                    .laborerId(pieceRate1.getId())
                    .weekStartDate(weekStart)
                    .weekEndDate(weekEnd)
                    .build());

            log.info("Labor Management data seeding completed.");
        }
    }
}
