package com.kalibyte.foundry.expenses.dto.response;

import com.kalibyte.foundry.expenses.entity.enums.ExpenseCategory;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseHeadResponse {
    private UUID id;

    private String name;

    private ExpenseCategory category;

    private String description;
}
