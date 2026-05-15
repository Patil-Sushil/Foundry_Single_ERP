package com.kalibyte.foundry.qa.defect.service;

import com.kalibyte.foundry.qa.common.enums.DefectCategory;
import com.kalibyte.foundry.qa.common.enums.Severity;
import com.kalibyte.foundry.qa.defect.entity.DefectCatalog;

import java.util.List;

public interface DefectCatalogService {
    List<DefectCatalog> list(DefectCategory category, Severity severity, Boolean isActive);
    DefectCatalog getById(Long id);
    DefectCatalog create(DefectCatalog defect);
    DefectCatalog update(Long id, DefectCatalog defectDetails);
    void deactivate(Long id);
}
