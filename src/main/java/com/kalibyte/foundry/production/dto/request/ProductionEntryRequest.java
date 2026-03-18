package com.kalibyte.foundry.production.dto.request;

import com.kalibyte.foundry.production.entity.enums.ProductionShift;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class ProductionEntryRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    private LocalDate reportDate;

    @NotNull
    private ProductionShift shift;

    private String operatorName;

    private String remarks;

    @NotNull
    private List<ProductionItemRequest> items;
}
