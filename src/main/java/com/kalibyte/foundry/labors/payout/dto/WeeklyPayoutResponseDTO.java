package com.kalibyte.foundry.labors.payout.dto;

import com.kalibyte.foundry.labors.payout.entity.Enum.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyPayoutResponseDTO {
    private Long id;
    private Long laborerId;
    private String laborerName;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private BigDecimal totalHours;
    private BigDecimal piecesCompleted;
    private BigDecimal grossPayout;
    private BigDecimal advanceDeduction;
    private BigDecimal netPayout;
    private PaymentStatus paymentStatus;
    private LocalDate paymentDate;
    private String paymentReference;
}
