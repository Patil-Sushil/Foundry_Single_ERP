package com.kalibyte.foundry.qa.defect.repository;

import com.kalibyte.foundry.qa.defect.entity.DefectCatalog;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DefectCatalogRepository extends JpaRepository<DefectCatalog, Long>, JpaSpecificationExecutor<DefectCatalog> {
    Optional<DefectCatalog> findByCode(String code);

    @Override
    List<DefectCatalog> findAll(Specification<DefectCatalog> spec);
}
