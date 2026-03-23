package com.kalibyte.foundry.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {

    private UUID id;
    private String orderNumber;

    private UUID customerId;
    private String customerName;

    private LocalDate orderDate;
    private LocalDate deliveryDate;

    private String status;

    private BigDecimal totalAmount;

    private List<OrderItemResponse> items;
}