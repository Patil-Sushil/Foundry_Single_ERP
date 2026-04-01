package com.kalibyte.foundry.qa.defect.repository;

import com.kalibyte.foundry.qa.common.enums.DefectCategory;
import com.kalibyte.foundry.qa.common.enums.Severity;
import com.kalibyte.foundry.qa.defect.entity.DefectCatalog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class DefectCatalogSpecification {

    private DefectCatalogSpecification() {}

    public static Specification<DefectCatalog> withFilters(
            DefectCategory category,
            Severity severity,
            Boolean isActive
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (severity != null) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            query.orderBy(cb.asc(root.get("code")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
