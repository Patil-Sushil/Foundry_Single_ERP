package com.kalibyte.foundry.expenses.dto.response;

import com.kalibyte.foundry.expenses.entity.enums.ExpensePaymentMode;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {

    private UUID id;

    private String expenseNumber;

    private UUID expenseHeadId;

    private String expenseHeadName;

    private BigDecimal amount;

    private LocalDate expenseDate;

    private ExpensePaymentMode paymentMode;

    private String referenceNumber;

    private String remarks;
}
