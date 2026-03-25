package com.kalibyte.foundry.order.dto.response;

import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import com.kalibyte.foundry.order.entity.enums.OrderType;
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
public class OrderResponse {

    private UUID id;
    private String orderNumber;

    private UUID customerId;
    private String customerName;

    private UUID quotationId;
    private String quotationNumber;

    private OrderType orderType;
    private OrderStatus status;

    private LocalDate orderDate;
    private LocalDate deliveryDate;

    private String placeOfSupply;
    private String poReference;

    private BigDecimal subTotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal totalAmount;

    private List<OrderItemResponse> items;

    // Summary
    private Integer totalQuantity;
    private Integer producedQuantity;
    private Integer dispatchedQuantity;
}