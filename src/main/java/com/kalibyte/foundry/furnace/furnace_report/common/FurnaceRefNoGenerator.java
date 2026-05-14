package com.kalibyte.foundry.furnace.furnace_report.common;

import com.kalibyte.foundry.furnace.furnace_report.repository.FurnaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class FurnaceRefNoGenerator {

    private final FurnaceRepository furnaceRepository;

    @Transactional
    public synchronized String generate() {
        int year = LocalDate.now().getYear();
        long count = furnaceRepository.countByYear(year);
        String furnaceRefNo;
        do {
            count++;
            furnaceRefNo = String.format("FR-%d-%04d", year, count);
        } while (furnaceRepository.existsByFurnaceRefNo(furnaceRefNo));
        return furnaceRefNo;
    }
}
