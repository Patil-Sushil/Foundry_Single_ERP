package com.kalibyte.foundry.qa.rejection.service.impl;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.qa.common.QaNumberGenerator;
import com.kalibyte.foundry.qa.common.enums.*;
import com.kalibyte.foundry.qa.defect.entity.DefectCatalog;
import com.kalibyte.foundry.qa.inspection.entity.InspectionFinding;
import com.kalibyte.foundry.qa.inspection.entity.QaInspection;
import com.kalibyte.foundry.qa.rejection.dto.QaRejectionResponse;
import com.kalibyte.foundry.qa.rejection.entity.QaRejection;
import com.kalibyte.foundry.qa.rejection.mapper.QaRejectionMapper;
import com.kalibyte.foundry.qa.rejection.repository.QaRejectionRepository;
import com.kalibyte.foundry.qa.rejection.repository.QaRejectionSpecification;
import com.kalibyte.foundry.qa.rejection.service.QaRejectionService;
import com.kalibyte.foundry.qa.tracking.service.QaTrackingLogService;
import com.kalibyte.foundry.scrap.dto.request.ScrapEntryRequest;
import com.kalibyte.foundry.scrap.dto.request.ScrapItemRequest;
import com.kalibyte.foundry.scrap.enums.ScrapSource;
import com.kalibyte.foundry.scrap.enums.ScrapStatus;
import com.kalibyte.foundry.scrap.service.ScrapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QaRejectionServiceImpl implements QaRejectionService {

    private final QaRejectionRepository repository;
    private final ScrapService scrapService;
    private final QaTrackingLogService trackingLogService;
    private final QaNumberGenerator numberGenerator;
    private final QaRejectionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<QaRejectionResponse> list(UUID orderId, RejectionStatus status, RejectionDisposition disposition) {
        Specification<QaRejection> spec = QaRejectionSpecification.withFilters(orderId, status, disposition);
        List<QaRejection> list = repository.findAll(spec);
        return mapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public QaRejectionResponse getById(Long id) {
        QaRejection rejection = repository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rejection not found: " + id));
        return mapper.toResponse(rejection);
    }

    @Override
    @Transactional
    public QaRejectionResponse createFromInspection(QaInspection inspection) {
        if (inspection.getTotalRejected() <= 0) {
            return null;
        }

        List<InspectionFinding> rejectFindings = inspection.getFindings().stream()
                .filter(f -> f.getDisposition() == FindingDisposition.REJECT)
                .toList();

        String defectSummary = rejectFindings.stream()
                .map(f -> f.getDefect().getName() + " (" + f.getQuantityAffected() + ")")
                .collect(Collectors.joining(", "));

        DefectCatalog primaryDefect = rejectFindings.stream()
                .max(Comparator.comparingInt(InspectionFinding::getQuantityAffected))
                .map(InspectionFinding::getDefect)
                .orElse(null);

        BigDecimal unitWeight = inspection.getOrderItem().getNetWeightKg();
        BigDecimal rejectedWeight = unitWeight != null ? unitWeight.multiply(BigDecimal.valueOf(inspection.getTotalRejected())) : BigDecimal.ZERO;

        QaRejection rejection = QaRejection.builder()
                .rejectionNumber(numberGenerator.generateRejectionNumber())
                .inspection(inspection)
                .productionEntryId(inspection.getProductionEntry().getId())
                .productionItemId(inspection.getProductionItem().getId())
                .order(inspection.getOrder())
                .orderItem(inspection.getOrderItem())
                .heatOrderItemId(inspection.getHeatOrderItem() != null ? inspection.getHeatOrderItem().getId() : null)
                .rejectedQuantity(inspection.getTotalRejected())
                .rejectedWeight(rejectedWeight)
                .unitWeight(unitWeight)
                .materialGrade(inspection.getOrderItem().getMaterialGrade())
                .primaryDefect(primaryDefect)
                .defectSummary(defectSummary)
                .disposition(RejectionDisposition.PENDING_REVIEW)
                .status(RejectionStatus.OPEN)
                .build();

        QaRejection saved = repository.save(rejection);
        trackingLogService.log(TrackingReferenceType.REJECTION, saved.getId(), null, saved.getStatus().name(), TrackingAction.CREATED, "SYSTEM", "Auto-created from Inspection " + inspection.getInspectionNumber());
        return mapper.toResponse(repository.findWithDetailsById(saved.getId()).get());
    }

    @Override
    @Transactional
    public QaRejectionResponse dispositionRejection(Long id, RejectionDisposition disposition, String remarks, String performedBy) {
        QaRejection rejection = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rejection not found: " + id));

        if (rejection.getStatus() == RejectionStatus.CLOSED || rejection.getStatus() == RejectionStatus.DISPOSITIONED) {
            throw new BusinessException("Rejection is already in a terminal state: " + rejection.getStatus());
        }

        String fromStatus = rejection.getStatus().name();
        rejection.setDisposition(disposition);
        rejection.setDispositionDate(LocalDate.now());
        rejection.setDispositionBy(performedBy);
        rejection.setDispositionRemarks(remarks);
        rejection.setStatus(RejectionStatus.DISPOSITIONED);

        if (disposition == RejectionDisposition.SCRAP_FOR_REMELT || disposition == RejectionDisposition.SCRAP_FOR_SALE) {
            createScrapFromRejection(rejection);
        }

        QaRejection saved = repository.save(rejection);
        trackingLogService.log(TrackingReferenceType.REJECTION, saved.getId(), fromStatus, saved.getStatus().name(), TrackingAction.DISPOSITIONED, performedBy, remarks);
        return mapper.toResponse(repository.findWithDetailsById(saved.getId()).get());
    }

    private void createScrapFromRejection(QaRejection rejection) {
        ScrapEntryRequest scrapRequest = ScrapEntryRequest.builder()
                .scrapSource(ScrapSource.PRODUCTION_REJECTION)
                .grade(rejection.getMaterialGrade())
                .totalWeight(rejection.getRejectedWeight())
                .qaRejectionId(rejection.getId())
                .rejectionNumber(rejection.getRejectionNumber())
                .heatId(rejection.getHeatOrderItemId()) // Use heatOrderItemId as reference if available
                .initialStatus(ScrapStatus.PENDING_VERIFICATION)
                .remarks("QA Rejection: " + rejection.getRejectionNumber() + ". Defects: " + rejection.getDefectSummary())
                .scrapItems(List.of(ScrapItemRequest.builder()
                        .itemName(rejection.getOrderItem().getPartName() + " (Rejected)")
                        .grade(rejection.getMaterialGrade())
                        .weight(rejection.getRejectedWeight())
                        .quantity(rejection.getRejectedQuantity())
                        .scrapType("PRODUCTION_REJECTION")
                        .recyclability("HIGH")
                        .build()))
                .build();

        var scrapResponse = scrapService.createScrapEntry(scrapRequest);
        rejection.setScrapEntryId(scrapResponse.getId());
        trackingLogService.log(TrackingReferenceType.REJECTION, rejection.getId(), rejection.getStatus().name(), rejection.getStatus().name(), TrackingAction.SCRAP_GENERATED, "SYSTEM", "Scrap entry created: " + scrapResponse.getScrapNumber());
    }
}
