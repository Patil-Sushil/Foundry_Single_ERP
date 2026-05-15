package com.kalibyte.foundry.labors.payout.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyPayoutRequest {
    private Long laborerId;
    @PastOrPresent(message = "Date should be in past or todays")
    private LocalDate weekStartDate;
    @FutureOrPresent(message = "Date should be of today or future")
    private LocalDate weekEndDate;
}
