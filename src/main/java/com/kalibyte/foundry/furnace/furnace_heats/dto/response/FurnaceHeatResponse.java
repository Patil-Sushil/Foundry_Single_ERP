package com.kalibyte.foundry.furnace.furnace_heats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FurnaceHeatResponse {
    private Long id;
    private double sipercentage;
    private double cpcpercentage;
    private double mgpercentage;
    private double startReading;
    private double stopReading;
    private double differenceReading;
    private double totalWeight;
    private double powerToWeight;
    private double pouringTemp;
    private List<HeatMaterialItemResponse> materialsUsed;
    private LocalTime pouringStartTime;
    private LocalTime pouringEndTime;
    private UUID orderId;

    private String grade;
    private BigDecimal liquidMetalWeight;
    private BigDecimal castingsPouredWeight;
    private BigDecimal runnerWeight;
    private BigDecimal riserWeight;
    private BigDecimal skullWeight;
    private BigDecimal spillageWeight;
    private BigDecimal slagWeight;
    private BigDecimal totalProcessScrap;
    private Long processScrapEntryId;
    private Boolean autoReturnScrap;
    private BigDecimal furnaceYieldPercentage;
    private BigDecimal pouringYieldPercentage;

    // Calculated fields
    private BigDecimal metalLoss; // Backward compatibility (alias for pouringLoss)
    private BigDecimal meltingLoss;
    private BigDecimal pouringLoss;
    private BigDecimal meltingLossPercentage;
    private BigDecimal pouringLossPercentage;
    private BigDecimal yieldPercentage;
    private BigDecimal remainingCapacity;

    private List<HeatOrderItemResponse> heatOrderItems;
}
