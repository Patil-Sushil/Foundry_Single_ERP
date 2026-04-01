package com.kalibyte.foundry.qa.customerreturn.repository;

import com.kalibyte.foundry.qa.common.enums.ReturnDisposition;
import com.kalibyte.foundry.qa.common.enums.ReturnStatus;
import com.kalibyte.foundry.qa.customerreturn.entity.CustomerReturn;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CustomerReturnSpecification {

    private CustomerReturnSpecification() {}

    public static Specification<CustomerReturn> withFilters(
            LocalDate startDate,
            LocalDate endDate,
            UUID customerId,
            UUID orderId,
            ReturnStatus status,
            ReturnDisposition disposition
    ) {
        return (root, query, cb) -> {
            // Fetch joins to prevent LazyInitializationException during mapping
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("customer", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("order", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("orderItem", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("inspection", jakarta.persistence.criteria.JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("returnDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("returnDate"), endDate));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            }
            if (orderId != null) {
                predicates.add(cb.equal(root.get("order").get("id"), orderId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (disposition != null) {
                predicates.add(cb.equal(root.get("disposition"), disposition));
            }

            query.distinct(true);
            query.orderBy(cb.desc(root.get("returnDate")), cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
