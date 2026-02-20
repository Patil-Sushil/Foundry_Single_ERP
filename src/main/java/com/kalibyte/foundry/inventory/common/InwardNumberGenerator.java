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

    @Transactional(readOnly = true)
    public String generate() {
        int year = LocalDate.now().getYear();
        long count = materialInwardRepository.countByYear(year);
        return String.format("MI-%d-%04d", year, count + 1);
    }
}
