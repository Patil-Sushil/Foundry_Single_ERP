package com.kalibyte.foundry.production.specification;

import com.kalibyte.foundry.production.entity.ProductionEntry;
import com.kalibyte.foundry.production.entity.enums.ProductionShift;
import com.kalibyte.foundry.production.entity.enums.ProductionStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProductionSpecification {

    private ProductionSpecification() {}

    public static Specification<ProductionEntry> withFilters(
            UUID orderId,
            LocalDate fromDate,
            LocalDate toDate,
            ProductionStatus status,
            ProductionShift shift
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // always exclude soft-deleted
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (orderId != null) {
                predicates.add(cb.equal(root.get("order").get("id"), orderId));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("reportDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("reportDate"), toDate));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (shift != null) {
                predicates.add(cb.equal(root.get("shift"), shift));
            }

            // default sort by reportDate DESC
            query.orderBy(cb.desc(root.get("reportDate")), cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}