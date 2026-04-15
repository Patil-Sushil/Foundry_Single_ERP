package com.kalibyte.foundry.order.validation;

import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class OrderStatusTransitionValidator {

    private OrderStatusTransitionValidator() {}

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        VALID_TRANSITIONS.put(OrderStatus.CREATED, Set.of(
                OrderStatus.CONFIRMED, OrderStatus.CANCELLED, OrderStatus.ON_HOLD, OrderStatus.IN_PRODUCTION));

        VALID_TRANSITIONS.put(OrderStatus.CONFIRMED, Set.of(
                OrderStatus.IN_PRODUCTION, OrderStatus.CANCELLED, OrderStatus.ON_HOLD));

        VALID_TRANSITIONS.put(OrderStatus.IN_PRODUCTION, Set.of(
                OrderStatus.PARTIALLY_PRODUCED, OrderStatus.PRODUCED, OrderStatus.ON_HOLD));

        VALID_TRANSITIONS.put(OrderStatus.PARTIALLY_PRODUCED, Set.of(
                OrderStatus.PRODUCED, OrderStatus.IN_PRODUCTION, OrderStatus.ON_HOLD));

        VALID_TRANSITIONS.put(OrderStatus.PRODUCED, Set.of(
                OrderStatus.PARTIALLY_DISPATCHED, OrderStatus.DISPATCHED));

        VALID_TRANSITIONS.put(OrderStatus.PARTIALLY_DISPATCHED, Set.of(
                OrderStatus.DISPATCHED));

        VALID_TRANSITIONS.put(OrderStatus.DISPATCHED, Set.of(
                OrderStatus.COMPLETED));

        VALID_TRANSITIONS.put(OrderStatus.ON_HOLD, Set.of(
                OrderStatus.CREATED, OrderStatus.CONFIRMED, OrderStatus.IN_PRODUCTION, OrderStatus.CANCELLED));

        VALID_TRANSITIONS.put(OrderStatus.COMPLETED, Set.of());
        VALID_TRANSITIONS.put(OrderStatus.CANCELLED, Set.of());
    }

    public static void validate(OrderStatus current, OrderStatus next) {
        if (current == next) return;

        Set<OrderStatus> validNextStatuses = VALID_TRANSITIONS.get(current);

        if (validNextStatuses == null || !validNextStatuses.contains(next)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Invalid status transition from %s to %s", current, next));
        }
    }

    public static Set<OrderStatus> getValidNextStatuses(OrderStatus current) {
        return VALID_TRANSITIONS.getOrDefault(current, Set.of());
    }
}