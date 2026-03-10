package com.kalibyte.foundry.accounts.util;

import com.kalibyte.foundry.accounts.repository.PaymentRepository;
import com.kalibyte.foundry.expenses.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentNumberGenerator {

    private final PaymentRepository paymentRepository;

    public String generate(){

        int year = Year.now().getValue();

        String prefix = "PAY-" + year + "-";

        String lastNumber = paymentRepository
                .findLastPaymentNumber()
                .orElse(null);

        int nextNumber = 1;

        if(lastNumber != null && lastNumber.startsWith(prefix)){

            String numberPart = lastNumber.substring(prefix.length());

            nextNumber = Integer.parseInt(numberPart) + 1;
        }

        return prefix + String.format("%04d", nextNumber);
    }
}
