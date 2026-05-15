package com.kalibyte.foundry.qa.inspection.service;

import com.kalibyte.foundry.production.entity.ProductionItem;
import com.kalibyte.foundry.qa.common.enums.InspectionResult;
import com.kalibyte.foundry.qa.common.enums.InspectionStage;
import com.kalibyte.foundry.qa.common.enums.InspectionStatus;
import com.kalibyte.foundry.qa.inspection.dto.QaInspectionResponse;
import com.kalibyte.foundry.qa.inspection.entity.QaInspection;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface QaInspectionService {
    List<QaInspectionResponse> list(LocalDate startDate, LocalDate endDate, UUID orderId, UUID productionEntryId, InspectionStage stage, InspectionResult result, InspectionStatus status);
    QaInspectionResponse getById(Long id);
    QaInspectionResponse createDraft(QaInspection inspection);
    QaInspectionResponse updateDraft(Long id, QaInspection updated);
    QaInspectionResponse completeInspection(Long id, String performedBy);
    QaInspectionResponse cancelInspection(Long id, String performedBy);
    QaInspectionResponse createDraftFromProduction(ProductionItem item);
}
