package com.kalibyte.foundry.furnace.furnace_heats.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FurnaceHeatRequest {
    private Long id;

    @Min(value = 0, message = "the si percentage too low it should be greater 0")
    @Max(value = 2, message = "the value is greater than 2 its unacceptable")
    private double sipercentage;

    @Min(value = 0, message = "the cpc percentage too low it should be greater 0")
    @Max(value = 5, message = "the value is greater than 5 its unacceptable")
    private double cpcpercentage;

//    @Min(value = 0, message = "the mg percentaget too low it should be greater 0")
//    @Max(value = 3, message = "the value is greater than 3 its unacceptable")
    private double mgpercentage;

    @Min(value = 0, message = "the electricity units too low it should be greater than 0 or greater")
    private double startReading;

    @Min(1)
    private double stopReading;

    private double differenceReading;
    @Min(1)
    private double totalWeight;

    private double powerToWeight;

    private double pouringTemp;

    @Valid
    private List<HeatMaterialItemRequest> materialsUsed;

    private LocalTime pouringStartTime;

    private LocalTime pouringEndTime;

    private UUID orderId;
}
