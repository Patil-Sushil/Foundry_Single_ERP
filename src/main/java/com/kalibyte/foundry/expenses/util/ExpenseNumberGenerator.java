package com.kalibyte.foundry.expenses.util;

import com.kalibyte.foundry.expenses.entity.Expense;
import com.kalibyte.foundry.expenses.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Year;


@Component
@RequiredArgsConstructor
public class ExpenseNumberGenerator {

    private final ExpenseRepository expenseRepository;

    public String generate() {

        int year = Year.now().getValue();

        String prefix = "EXP-" + year + "-";

        Expense lastExpense =
                expenseRepository.findTopByOrderByCreatedAtDesc()
                        .orElse(null);

        int nextNumber = 1;

        if (lastExpense != null) {

            String lastNumber = lastExpense.getExpenseNumber();

            if (lastNumber != null && lastNumber.startsWith(prefix)) {

                String numberPart =
                        lastNumber.substring(prefix.length());

                nextNumber = Integer.parseInt(numberPart) + 1;
            }
        }

        return prefix + String.format("%04d", nextNumber);
    }
}