package com.kalibyte.foundry.furnace.furnace_heats.dto.request;

import com.kalibyte.foundry.furnace.furnace_heats.entity.Enum.HeatMaterialType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeatMaterialItemRequest {
    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Quantity used is required")
    @Positive(message = "Quantity used must be positive")
    private Double quantityUsed;

    @NotNull(message = "Material type is required")
    @Builder.Default
    private HeatMaterialType materialType = HeatMaterialType.RAW_MATERIAL;
}
