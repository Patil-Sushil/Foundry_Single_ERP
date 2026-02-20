package com.kalibyte.foundry.inventory.common;

import com.kalibyte.foundry.inventory.purchaseorder.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PONumberGenerator {

    private final PurchaseOrderRepository purchaseOrderRepository;

    @Transactional(readOnly = true)
    public String generate() {
        int year = LocalDate.now().getYear();
        long count = purchaseOrderRepository.countByYear(year);
        return String.format("PO-%d-%04d", year, count + 1);
    }
}
