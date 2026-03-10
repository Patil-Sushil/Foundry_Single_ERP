package com.kalibyte.foundry.expenses.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.expenses.entity.enums.ExpensePaymentMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Expense extends BaseEntity {
    @Column(name = "expense_number", nullable = false, unique = true)
    private String expenseNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_head_id", nullable = false)
    private ExpenseHead expenseHead;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    private ExpensePaymentMode paymentMode;

    private String referenceNumber; // bill no, etc.

    private String remarks;
}
