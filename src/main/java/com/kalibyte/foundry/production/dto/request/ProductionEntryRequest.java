package com.kalibyte.foundry.production.dto.request;

import com.kalibyte.foundry.production.entity.enums.ProductionShift;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class ProductionEntryRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Report date is required")
    @PastOrPresent(message = "Report date cannot be in the future")
    private LocalDate reportDate;

    @NotNull(message = "Shift is required")
    private ProductionShift shift;

    private String operatorName;

    private String remarks;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<ProductionItemRequest> items;
}