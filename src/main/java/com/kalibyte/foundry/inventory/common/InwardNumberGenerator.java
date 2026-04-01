package com.kalibyte.foundry.inventory.common;

import com.kalibyte.foundry.inventory.inward.repository.MaterialInwardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class InwardNumberGenerator {

    private final MaterialInwardRepository materialInwardRepository;

    @Transactional
    public String generate() {
        int year = LocalDate.now().getYear();
        long count = materialInwardRepository.countByYear(year);
        String inwardNumber;
        do {
            count++;
            inwardNumber = String.format("MI-%d-%04d", year, count);
        } while (materialInwardRepository.existsByInwardNumber(inwardNumber));
        return inwardNumber;
    }
}
