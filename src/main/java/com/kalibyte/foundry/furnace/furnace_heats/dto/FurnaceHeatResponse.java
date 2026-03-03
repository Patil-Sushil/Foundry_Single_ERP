package com.kalibyte.foundry.furnace.furnace_heats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

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
    private String orderId;
}
