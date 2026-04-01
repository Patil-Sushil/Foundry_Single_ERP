package com.kalibyte.foundry.labors.labor.dto;

import com.kalibyte.foundry.labors.labor.entity.Enum.WageType;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaborerRequestDTO {
    private String name;
    private WageType wageType;
    private BigDecimal dailyWage;
    private BigDecimal pieceRate;
    private BigDecimal hourlyRate;
    private Boolean isActive;
}
