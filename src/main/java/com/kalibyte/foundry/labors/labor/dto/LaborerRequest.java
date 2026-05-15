package com.kalibyte.foundry.labors.labor.dto;

import com.kalibyte.foundry.labors.labor.entity.Enum.WageType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaborerRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String phNumber;
    private String address;
    @Email
    private String email;

    @NotNull(message = "Wage type is required")
    private WageType wageType;

    private BigDecimal dailyWage;
    private BigDecimal pieceRate;
    private BigDecimal hourlyRate;
    private Boolean isActive;
}
