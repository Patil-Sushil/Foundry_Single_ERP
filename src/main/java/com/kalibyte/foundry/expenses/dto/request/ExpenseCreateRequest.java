package com.kalibyte.foundry.expenses.dto.request;

import com.kalibyte.foundry.expenses.entity.enums.ExpenseCategory;
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
public class ExpenseCreateRequest {

    private UUID expenseHeadId;

    private String expenseHeadName;

    private ExpenseCategory category;

    private BigDecimal amount;

    private String description;

    private LocalDate expenseDate;

    private ExpensePaymentMode paymentMode;

    private String referenceNumber;

    private String remarks;
}