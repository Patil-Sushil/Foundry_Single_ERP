package com.kalibyte.foundry.order.specification;

import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.ENUM.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class OrderSpecification {

    public static Specification<Order> filter(
            OrderStatus status,
            UUID customerId,
            LocalDate from,
            LocalDate to
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));

            if (customerId != null)
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));

            if (from != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), from));

            if (to != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), to));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}