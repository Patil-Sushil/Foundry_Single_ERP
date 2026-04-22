package com.kalibyte.foundry.billing.deliveryChallan.dto.request;

import com.kalibyte.foundry.order.entity.OrderItem;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryChallanItemRequest {

    @NotNull(message = "Order item ID is required")
    private UUID orderItemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    private BigDecimal weight;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.00", message = "Rate cannot be negative")
    private BigDecimal rate;

    public OrderItem getOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(orderItemId);
        return orderItem;
    }
}