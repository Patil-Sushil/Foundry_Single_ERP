package com.kalibyte.foundry.furnace.furnace_heats.service;

import com.kalibyte.foundry.furnace.furnace_heats.entity.ElectricityRate;

public interface ElectricityRateService {
    Double getCurrentRate();
    ElectricityRate updateRate(Double newRate);
}
