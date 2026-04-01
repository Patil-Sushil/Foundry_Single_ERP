package com.kalibyte.foundry.qa.inspection.repository;

import com.kalibyte.foundry.qa.common.enums.InspectionResult;
import com.kalibyte.foundry.qa.common.enums.InspectionStage;
import com.kalibyte.foundry.qa.common.enums.InspectionStatus;
import com.kalibyte.foundry.qa.inspection.entity.QaInspection;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class QaInspectionSpecification {

    private QaInspectionSpecification() {}

    public static Specification<QaInspection> withFilters(
            LocalDate startDate,
            LocalDate endDate,
            UUID orderId,
            UUID productionEntryId,
            InspectionStage inspectionStage,
            InspectionResult result,
            InspectionStatus status
    ) {
        return (root, query, cb) -> {
            // Fetch joins to prevent LazyInitializationException during mapping
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("order", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("orderItem", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("productionEntry", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("productionItem", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("heatOrderItem", jakarta.persistence.criteria.JoinType.LEFT);
                
                jakarta.persistence.criteria.Fetch<QaInspection, ?> findingsFetch = root.fetch("findings", jakarta.persistence.criteria.JoinType.LEFT);
                findingsFetch.fetch("defect", jakarta.persistence.criteria.JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("inspectionDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("inspectionDate"), endDate));
            }
            if (orderId != null) {
                predicates.add(cb.equal(root.get("order").get("id"), orderId));
            }
            if (productionEntryId != null) {
                predicates.add(cb.equal(root.get("productionEntry").get("id"), productionEntryId));
            }
            if (inspectionStage != null) {
                predicates.add(cb.equal(root.get("inspectionStage"), inspectionStage));
            }
            if (result != null) {
                predicates.add(cb.equal(root.get("result"), result));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            query.distinct(true);
            query.orderBy(cb.desc(root.get("inspectionDate")), cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
