package com.kalibyte.foundry.furnace.furnace_heats.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElectricityRateRequest {
    @NotNull(message = "Rate per unit is required")
    @Positive(message = "Rate must be positive")
    private Double ratePerUnit;
}
