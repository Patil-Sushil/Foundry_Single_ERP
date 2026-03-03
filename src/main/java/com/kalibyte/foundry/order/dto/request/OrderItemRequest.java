package com.kalibyte.foundry.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemRequest {

    @NotBlank
    private String productName;

    @NotBlank
    private String metalType;

    @NotNull
    private BigDecimal quantity;

    @NotNull
    private BigDecimal unitPrice;
}