package com.kalibyte.foundry.furnace.furnace_heats.dto.request;

import com.kalibyte.foundry.furnace.furnace_heats.validation.ValidMetalBalance;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@ValidMetalBalance
public class FurnaceHeatRequest {
    private Long id;

    @Min(value = 0, message = "the si percentage too low it should be greater 0")
    @Max(value = 2, message = "the value is greater than 2 its unacceptable")
    private Double sipercentage;

    @Min(value = 0, message = "the cpc percentage too low it should be greater 0")
    @Max(value = 5, message = "the value is greater than 5 its unacceptable")
    private Double cpcpercentage;

//    @Min(value = 0, message = "the mg percentage too low it should be greater 0")
//    @Max(value = 3, message = "the value is greater than 3 its unacceptable")
    private Double mgpercentage;

    @Min(value = 0, message = "the electricity units too low it should be greater than 0 ")
    private Double startReading;

    @Min(1)
    private Double stopReading;

    private Double differenceReading;
    @Min(1)
    private Double totalWeight;

    private Double powerToWeight;

    private Double pouringTemp;

    private List<@Valid HeatMaterialItemRequest> materialsUsed;

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

    private Boolean autoReturnScrap;

    private List<@Valid HeatOrderItemRequest> heatOrderItems;
}
