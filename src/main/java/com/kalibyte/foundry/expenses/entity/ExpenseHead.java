package com.kalibyte.foundry.expenses.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.expenses.entity.enums.ExpenseCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "expense_heads")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseHead extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    private String description;
}
