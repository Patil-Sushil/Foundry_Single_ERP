package com.kalibyte.foundry.furnace.furnace_heats.dto;

import com.kalibyte.foundry.furnace.furnace_heats.entity.Enum.HeatMaterialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeatMaterialItemResponse {
    private Long id;
    private Long itemId;
    private String itemName;
    private HeatMaterialType materialType;
    private Double quantityUsed;
    private Double unitRate;
    private Double totalCost;
}
