package com.kalibyte.foundry.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {

    private UUID quotationId;

    private UUID customerId;

    @NotNull(message = "Delivery date is required")
    private LocalDate deliveryDate;

    private String placeOfSupply;
    private String poReference;

    private BigDecimal discount;
    private BigDecimal tax;

    @Valid
    private List<OrderItemRequest> items;
}