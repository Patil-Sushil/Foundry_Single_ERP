package com.kalibyte.foundry.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "Delivery date is required")
    private LocalDate deliveryDate;

    @Size(max = 150)
    private String placeOfSupply;

    @Size(max = 150)
    private String poReference;

    @Valid
    private List<OrderItemRequest> items;
}