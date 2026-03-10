package com.kalibyte.foundry.billing.dto.request;

import com.kalibyte.foundry.order.entity.OrderItem;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryChallanItemRequest {

    private UUID orderItemId;

    private Integer quantity;

    private BigDecimal weight;

    private BigDecimal rate;


    public OrderItem getOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(orderItemId);
        return orderItem;
    }
}