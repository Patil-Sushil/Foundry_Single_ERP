package com.kalibyte.foundry.labors.payout.service;

import com.kalibyte.foundry.labors.advance.entity.AdvanceTransaction;
import com.kalibyte.foundry.labors.advance.entity.Enum.TransactionType;
import com.kalibyte.foundry.labors.advance.repository.AdvanceTransactionRepository;
import com.kalibyte.foundry.labors.attendance.entity.Attendance;
import com.kalibyte.foundry.labors.attendance.repository.AttendanceRepository;
import com.kalibyte.foundry.labors.labor.entity.Laborer;
import com.kalibyte.foundry.labors.labor.repository.LaborerRepository;
import com.kalibyte.foundry.labors.payout.dto.DisbursePayoutRequestDTO;
import com.kalibyte.foundry.labors.payout.dto.WeeklyPayoutRequestDTO;
import com.kalibyte.foundry.labors.payout.dto.WeeklyPayoutResponseDTO;
import com.kalibyte.foundry.labors.payout.entity.Enum.PaymentStatus;
import com.kalibyte.foundry.labors.payout.entity.WeeklyPayout;
import com.kalibyte.foundry.labors.payout.exception.PayoutException;
import com.kalibyte.foundry.labors.payout.repository.WeeklyPayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyPayoutService {

    private final WeeklyPayoutRepository weeklyPayoutRepository;
    private final AttendanceRepository attendanceRepository;
    private final AdvanceTransactionRepository advanceTransactionRepository;
    private final LaborerRepository laborerRepository;

    @Transactional
    public WeeklyPayoutResponseDTO generateWeeklyPayout(WeeklyPayoutRequestDTO request) {

        Optional<WeeklyPayout> weeklyPayout = weeklyPayoutRepository.findByLaborerIdAndWeekStartDateAndWeekEndDate(request.getLaborerId(),request.getWeekStartDate(),request.getWeekEndDate());
        if((weeklyPayout.isPresent())){
            throw new PayoutException("Payment for this week is already present");
        }
        Laborer laborer = laborerRepository.findById(request.getLaborerId())
                .orElseThrow(() -> new RuntimeException("Laborer not found"));

        List<Attendance> attendances = attendanceRepository.findByLaborerIdAndWorkDateBetween(
                request.getLaborerId(), request.getWeekStartDate(), request.getWeekEndDate());

        BigDecimal totalHours = attendances.stream()
                .map(a -> a.getHoursWorked() != null ? a.getHoursWorked() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossPayout = attendances.stream()
                .map(Attendance::getEarnedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal piecesCompleted = attendances.stream()
                .map(a -> Optional.ofNullable(a.getPiecesCompleted())
                        .map(BigDecimal::valueOf)
                        .orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal outstandingAdvance = advanceTransactionRepository.getOutstandingBalance(request.getLaborerId());
        BigDecimal deduction = grossPayout.min(outstandingAdvance);
        BigDecimal netPayout = grossPayout.subtract(deduction);

        WeeklyPayout payout = weeklyPayoutRepository.findByLaborerIdAndWeekStartDateAndWeekEndDate(
                request.getLaborerId(), request.getWeekStartDate(), request.getWeekEndDate())
                .orElse(new WeeklyPayout());

        payout.setLaborer(laborer);
        payout.setWeekStartDate(request.getWeekStartDate());
        payout.setWeekEndDate(request.getWeekEndDate());
        payout.setTotalHours(totalHours);
        payout.setGrossPayout(grossPayout);
        payout.setPiecesCompleted(piecesCompleted);
        payout.setAdvanceDeduction(deduction);
        payout.setNetPayout(netPayout);
        payout.setPaymentStatus(PaymentStatus.PENDING);

        payout = weeklyPayoutRepository.save(payout);

        if (deduction.compareTo(BigDecimal.ZERO) > 0) {
            AdvanceTransaction transaction = AdvanceTransaction.builder()
                    .laborerId(request.getLaborerId())
                    .transactionDate(LocalDate.now())
                    .amount(deduction)
                    .transactionType(TransactionType.DEDUCTED)
                    .notes("Deducted from weekly payout " + request.getWeekStartDate() + " to " + request.getWeekEndDate())
                    .build();
            advanceTransactionRepository.save(transaction);
        }

        return mapToResponse(payout);
    }

    @Transactional
    public WeeklyPayoutResponseDTO disbursePayout(Long payoutId, DisbursePayoutRequestDTO request) {
        WeeklyPayout payout = weeklyPayoutRepository.findById(payoutId)
                .orElseThrow(() -> new RuntimeException("Weekly payout not found"));

        if (payout.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Payout already processed");
        }

        payout.setPaymentStatus(PaymentStatus.PAID);
        payout.setPaymentDate(request.getPaymentDate());
        payout.setPaymentReference(request.getPaymentReference());

        payout = weeklyPayoutRepository.save(payout);
        return mapToResponse(payout);
    }

    public List<WeeklyPayoutResponseDTO> getPayoutsByLaborer(Long laborerId) {
        return weeklyPayoutRepository.findByLaborerId(laborerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private WeeklyPayoutResponseDTO mapToResponse(WeeklyPayout payout) {
        return WeeklyPayoutResponseDTO.builder()
                .id(payout.getId())
                .laborerId(payout.getLaborer().getId())
                .laborerName(payout.getLaborer().getName())
                .weekStartDate(payout.getWeekStartDate())
                .weekEndDate(payout.getWeekEndDate())
                .totalHours(payout.getTotalHours())
                .grossPayout(payout.getGrossPayout())
                .piecesCompleted(payout.getPiecesCompleted())
                .advanceDeduction(payout.getAdvanceDeduction())
                .netPayout(payout.getNetPayout())
                .paymentStatus(payout.getPaymentStatus())
                .paymentDate(payout.getPaymentDate())
                .paymentReference(payout.getPaymentReference())
                .build();
    }
}
