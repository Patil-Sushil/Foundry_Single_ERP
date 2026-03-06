package com.kalibyte.foundry.order.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class OrderItemResponse {

    private UUID id;
    private String productName;
    private String metalType;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}