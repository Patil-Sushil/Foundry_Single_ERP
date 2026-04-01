package com.kalibyte.foundry.furnace.furnace_heats.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.furnace.furnace_heats.entity.ElectricityRate;
import com.kalibyte.foundry.furnace.furnace_heats.repository.ElectricityRateRepository;
import com.kalibyte.foundry.furnace.furnace_heats.service.ElectricityRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ElectricityRateServiceImpl implements ElectricityRateService {

    private final ElectricityRateRepository electricityRateRepository;

    @Override
    public Double getCurrentRate() {
        return electricityRateRepository.findByActiveTrue()
                .map(ElectricityRate::getRatePerUnit)
                .orElseThrow(() -> new ResourceNotFoundException("Active electricity rate not found"));
    }

    @Override
    @Transactional
    public ElectricityRate updateRate(Double newRate) {
        electricityRateRepository.findByActiveTrue()
                .ifPresent(rate -> {
                    rate.setActive(false);
                    rate.setEffectiveTo(LocalDate.now());
                    electricityRateRepository.save(rate);
                });

        ElectricityRate nextRate = ElectricityRate.builder()
                .ratePerUnit(newRate)
                .effectiveFrom(LocalDate.now())
                .active(true)
                .build();

        return electricityRateRepository.save(nextRate);
    }
}
