package com.kalibyte.foundry.order.dto.request;

import com.kalibyte.foundry.inventory.purchaseorder.dto.request.PurchaseOrderItemRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
public class OrderCreateRequest {

    private UUID quotationId;
    private UUID customerId;

    @NotNull
    private LocalDate deliveryDate;

    private List<OrderItemRequest> items;
}