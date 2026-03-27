package com.kalibyte.foundry.order.specification;

import com.kalibyte.foundry.enquiry.entity.enums.MetalType;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderItemSpecification {

    private OrderItemSpecification() {}

    public static Specification<OrderItem> filter(
            UUID orderId,
            UUID customerId,
            OrderStatus orderStatus,
            String partName,
            MetalType metalType,
            String castingProcess,
            Boolean pendingOnly) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Join with Order (always needed for order-level filters)
            Join<OrderItem, Order> orderJoin = root.join("order", JoinType.INNER);

            // Filter by specific order
            if (orderId != null) {
                predicates.add(cb.equal(orderJoin.get("id"), orderId));
            }

            // Filter by customer
            if (customerId != null) {
                predicates.add(cb.equal(
                        orderJoin.get("customer").get("id"), customerId));
            }

            // Filter by order status
            if (orderStatus != null) {
                predicates.add(cb.equal(orderJoin.get("status"), orderStatus));
            }

            // Filter by part name (case-insensitive search)
            if (partName != null && !partName.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("partName")),
                        "%" + partName.toLowerCase() + "%"));
            }

            // Filter by metal type
            if (metalType != null) {
                predicates.add(cb.equal(root.get("metalType"), metalType));
            }

            // Filter by casting process
            if (castingProcess != null && !castingProcess.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("castingProcess")),
                        "%" + castingProcess.toLowerCase() + "%"));
            }

            // Filter pending items only (produced < quantity)
            if (Boolean.TRUE.equals(pendingOnly)) {
                predicates.add(cb.lessThan(
                        cb.coalesce(root.get("producedQuantity"), 0),
                        root.get("quantity")));

                // Exclude cancelled and completed orders
                predicates.add(cb.notEqual(orderJoin.get("status"), OrderStatus.CANCELLED));
                predicates.add(cb.notEqual(orderJoin.get("status"), OrderStatus.COMPLETED));
            }

            // Default ordering by order date descending
            query.orderBy(cb.desc(orderJoin.get("orderDate")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}