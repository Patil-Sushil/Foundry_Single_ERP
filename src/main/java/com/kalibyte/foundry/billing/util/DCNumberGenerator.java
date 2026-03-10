package com.kalibyte.foundry.billing.util;

import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallan;
import com.kalibyte.foundry.billing.deliveryChallan.repository.DeliveryChallanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DCNumberGenerator {

    private final DeliveryChallanRepository repository;

    public String generateDCNumber() {

        int year = Year.now().getValue();

        String prefix = "DC-" + year + "-";

        Optional<DeliveryChallan> lastDC =
                repository.findTopByDcNumberStartingWithOrderByDcNumberDesc(prefix);

        int nextNumber = 1;

        if (lastDC.isPresent()) {

            String lastNumber = lastDC.get().getDcNumber();

            String sequence = lastNumber.substring(prefix.length());

            nextNumber = Integer.parseInt(sequence) + 1;
        }

        return prefix + String.format("%05d", nextNumber);
    }
}
