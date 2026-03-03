package com.kalibyte.foundry.order.dto.response;

import com.kalibyte.foundry.order.entity.ENUM.OrderStatus;
import com.kalibyte.foundry.order.entity.ENUM.OrderType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class OrderResponse {

    private UUID id;
    private String orderNumber;
    private OrderStatus status;

    private OrderType orderType;

    private LocalDate orderDate;
    private LocalDate deliveryDate;

    private CustomerSummary customer;
    private QuotationSummary quotation;

    private List<OrderItemResponse> items;

    private BigDecimal totalAmount;

    private LocalDateTime createdAt;
    private String createdBy;
}