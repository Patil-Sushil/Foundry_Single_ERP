package com.kalibyte.foundry.qa.rejection.repository;

import com.kalibyte.foundry.qa.common.enums.RejectionDisposition;
import com.kalibyte.foundry.qa.common.enums.RejectionStatus;
import com.kalibyte.foundry.qa.rejection.entity.QaRejection;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class QaRejectionSpecification {

    private QaRejectionSpecification() {}

    public static Specification<QaRejection> withFilters(
            UUID orderId,
            RejectionStatus status,
            RejectionDisposition disposition
    ) {
        return (root, query, cb) -> {
            // Fetch joins to prevent LazyInitializationException during mapping
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("order", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("orderItem", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("inspection", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("primaryDefect", jakarta.persistence.criteria.JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

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
            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
