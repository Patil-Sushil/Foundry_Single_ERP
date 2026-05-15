package com.kalibyte.foundry.qa.defect.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.qa.common.enums.DefectCategory;
import com.kalibyte.foundry.qa.common.enums.Severity;
import com.kalibyte.foundry.qa.defect.entity.DefectCatalog;
import com.kalibyte.foundry.qa.defect.repository.DefectCatalogRepository;
import com.kalibyte.foundry.qa.defect.repository.DefectCatalogSpecification;
import com.kalibyte.foundry.qa.defect.service.DefectCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefectCatalogServiceImpl implements DefectCatalogService {

    private final DefectCatalogRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<DefectCatalog> list(DefectCategory category, Severity severity, Boolean isActive) {
        Specification<DefectCatalog> spec = DefectCatalogSpecification.withFilters(category, severity, isActive);
        return repository.findAll(spec);
    }

    @Override
    @Transactional(readOnly = true)
    public DefectCatalog getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Defect not found: " + id));
    }

    @Override
    @Transactional
    public DefectCatalog create(DefectCatalog defect) {
        return repository.save(defect);
    }

    @Override
    @Transactional
    public DefectCatalog update(Long id, DefectCatalog defectDetails) {
        DefectCatalog existing = getById(id);
        existing.setName(defectDetails.getName());
        existing.setCategory(defectDetails.getCategory());
        existing.setSeverity(defectDetails.getSeverity());
        existing.setDescription(defectDetails.getDescription());
        existing.setIsActive(defectDetails.getIsActive());
        return repository.save(existing);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        DefectCatalog existing = getById(id);
        existing.setIsActive(false);
        repository.save(existing);
    }
}
