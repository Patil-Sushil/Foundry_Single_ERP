package com.kalibyte.foundry.inventory.purchaseorder.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record CreatePurchaseOrderRequest(
    @NotNull(message = "Vendor ID is required")
    Long vendorId,

    LocalDate expectedDeliveryDate,

    @NotEmpty(message = "Order items cannot be empty")
    List<@Valid PurchaseOrderItemRequest> items,

    String notes
) implements Serializable {}
