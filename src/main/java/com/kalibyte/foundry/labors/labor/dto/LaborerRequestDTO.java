package com.kalibyte.foundry.labors.labor.dto;

import com.kalibyte.foundry.labors.labor.entity.Enum.WageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaborerRequestDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Wage type is required")
    private WageType wageType;

    private BigDecimal dailyWage;
    private BigDecimal pieceRate;
    private BigDecimal hourlyRate;
    private Boolean isActive;
}
