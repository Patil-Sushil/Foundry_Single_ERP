package com.kalibyte.foundry.order.validation;

import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class OrderStatusTransitionValidator {

    public static void validate(OrderStatus current, OrderStatus next) {

        if (current == OrderStatus.COMPLETED || current == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot change status after completion or cancellation"
            );
        }

        if (current == next) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order already in this status"
            );
        }
    }
}