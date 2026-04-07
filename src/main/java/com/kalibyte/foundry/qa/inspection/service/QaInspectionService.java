package com.kalibyte.foundry.qa.inspection.service;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.furnace.furnace_heats.repository.HeatOrderItemRepository;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.order.repository.OrderItemRepository;
import com.kalibyte.foundry.order.repository.OrderRepository;
import com.kalibyte.foundry.production.entity.ProductionEntry;
import com.kalibyte.foundry.production.entity.ProductionItem;
import com.kalibyte.foundry.production.repository.ProductionEntryRepository;
import com.kalibyte.foundry.production.repository.ProductionItemRepository;
import com.kalibyte.foundry.qa.common.QaNumberGenerator;
import com.kalibyte.foundry.qa.common.enums.*;
import com.kalibyte.foundry.qa.defect.entity.DefectCatalog;
import com.kalibyte.foundry.qa.defect.service.DefectCatalogService;
import com.kalibyte.foundry.qa.inspection.dto.QaInspectionResponse;
import com.kalibyte.foundry.qa.inspection.entity.InspectionFinding;
import com.kalibyte.foundry.qa.inspection.entity.QaInspection;
import com.kalibyte.foundry.qa.inspection.mapper.QaInspectionMapper;
import com.kalibyte.foundry.qa.inspection.repository.QaInspectionRepository;
import com.kalibyte.foundry.qa.inspection.repository.QaInspectionSpecification;
import com.kalibyte.foundry.qa.rejection.service.QaRejectionService;
import com.kalibyte.foundry.qa.tracking.service.QaTrackingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QaInspectionService {

    private final QaInspectionRepository repository;
    private final ProductionEntryRepository productionEntryRepository;
    private final ProductionItemRepository productionItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final HeatOrderItemRepository heatOrderItemRepository;
    private final DefectCatalogService defectCatalogService;
    private final QaRejectionService rejectionService;
    private final QaTrackingLogService trackingLogService;
    private final QaNumberGenerator numberGenerator;
    private final QaInspectionMapper mapper;

    @Transactional(readOnly = true)
    public List<QaInspectionResponse> list(LocalDate startDate, LocalDate endDate, UUID orderId, UUID productionEntryId, InspectionStage stage, InspectionResult result, InspectionStatus status) {
        Specification<QaInspection> spec = QaInspectionSpecification.withFilters(startDate, endDate, orderId, productionEntryId, stage, result, status);
        List<QaInspection> list = repository.findAll(spec);
        return mapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    public QaInspectionResponse getById(Long id) {
        QaInspection inspection = repository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection not found: " + id));
        return mapper.toResponse(inspection);
    }

    @Transactional
    public QaInspectionResponse createDraft(QaInspection inspection) {
        inspection.setInspectionNumber(numberGenerator.generateInspectionNumber());
        inspection.setStatus(InspectionStatus.DRAFT);
        inspection.setResult(InspectionResult.PENDING);
        
        // Ensure all entities are loaded
        fetchRelatedEntities(inspection);
        
        QaInspection saved = repository.save(inspection);
        // Map inside transaction to avoid lazy loading issues
        return mapper.toResponse(repository.findWithDetailsById(saved.getId()).get());
    }

    @Transactional
    public QaInspectionResponse updateDraft(Long id, QaInspection updated) {
        QaInspection existing = repository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection not found: " + id));
        
        if (existing.getStatus() != InspectionStatus.DRAFT) {
            throw new BusinessException("Only DRAFT inspections can be updated.");
        }
        
        existing.setInspectionDate(updated.getInspectionDate());
        existing.setInspectorName(updated.getInspectorName());
        existing.setInspectionStage(updated.getInspectionStage());
        existing.setInspectionType(updated.getInspectionType());
        existing.setTotalInspected(updated.getTotalInspected());
        existing.setRemarks(updated.getRemarks());
        
        // Update references if they are provided
        if (updated.getProductionEntry() != null) existing.setProductionEntry(updated.getProductionEntry());
        if (updated.getProductionItem() != null) existing.setProductionItem(updated.getProductionItem());
        if (updated.getOrder() != null) existing.setOrder(updated.getOrder());
        if (updated.getOrderItem() != null) existing.setOrderItem(updated.getOrderItem());
        if (updated.getHeatOrderItem() != null) existing.setHeatOrderItem(updated.getHeatOrderItem());

        // Ensure all entities are loaded
        fetchRelatedEntities(existing);
        
        // Update findings
        if (updated.getFindings() != null) {
            existing.getFindings().clear();
            for (InspectionFinding finding : updated.getFindings()) {
                if (finding.getDefect() != null && finding.getDefect().getId() != null) {
                    finding.setDefect(defectCatalogService.getById(finding.getDefect().getId()));
                }
                existing.addFinding(finding);
            }
        }
        
        QaInspection saved = repository.save(existing);
        return mapper.toResponse(repository.findWithDetailsById(saved.getId()).get());
    }

    @Transactional
    public QaInspectionResponse completeInspection(Long id, String performedBy) {
        QaInspection inspection = repository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection not found: " + id));
        
        if (inspection.getStatus() != InspectionStatus.DRAFT) {
            throw new BusinessException("Inspection is already " + inspection.getStatus());
        }

        // 1. Recalculate totals from findings
        int totalRejected = 0;
        int totalReworkable = 0;
        
        for (InspectionFinding finding : inspection.getFindings()) {
            if (finding.getDisposition() == FindingDisposition.REJECT) {
                totalRejected += finding.getQuantityAffected();
            } else if (finding.getDisposition() == FindingDisposition.REWORK) {
                totalReworkable += finding.getQuantityAffected();
            }
        }
        
        int totalAccepted = inspection.getTotalInspected() - totalRejected - totalReworkable;
        if (totalAccepted < 0) {
            throw new BusinessException("Total rejected and reworkable exceeds total inspected.");
        }
        
        inspection.setTotalAccepted(totalAccepted);
        inspection.setTotalRejected(totalRejected);
        inspection.setTotalReworkable(totalReworkable);

        // 2. Set inspection result
        if (totalRejected == 0 && totalReworkable == 0) {
            inspection.setResult(InspectionResult.PASSED);
        } else if (totalRejected == inspection.getTotalInspected()) {
            inspection.setResult(InspectionResult.FAILED);
        } else {
            inspection.setResult(InspectionResult.CONDITIONAL_PASS);
        }

        // 3. Set status to COMPLETED
        String fromStatus = inspection.getStatus().name();
        inspection.setStatus(InspectionStatus.COMPLETED);

        // 4. Update the ProductionItem
        ProductionItem prodItem = inspection.getProductionItem();
        prodItem.setInspectedQuantity(prodItem.getInspectedQuantity() + inspection.getTotalInspected());
        prodItem.setAcceptedQuantity(prodItem.getAcceptedQuantity() + inspection.getTotalAccepted());
        prodItem.setRejectedQuantity(prodItem.getRejectedQuantity() + inspection.getTotalRejected());
        prodItem.setReworkQuantity(prodItem.getReworkQuantity() + inspection.getTotalReworkable());

        // 5. CRITICAL — AUTO-UPDATE DISPATCH
        prodItem.setDispatchedQuantity(prodItem.getAcceptedQuantity());

        // 6. Recalculate the parent ProductionEntry totals
        ProductionEntry prodEntry = inspection.getProductionEntry();
        prodEntry.recalculateTotals();
        
        productionItemRepository.save(prodItem);
        productionEntryRepository.save(prodEntry);

        // 7. If totalRejected > 0, auto-create Rejection
        if (totalRejected > 0) {
            rejectionService.createFromInspection(inspection);
        }

        QaInspection saved = repository.save(inspection);
        
        // 8. Log to QaTrackingLog
        trackingLogService.log(TrackingReferenceType.INSPECTION, saved.getId(), fromStatus, saved.getStatus().name(), TrackingAction.INSPECTED, performedBy, "Inspection completed.");
        
        return mapper.toResponse(repository.findWithDetailsById(saved.getId()).get());
    }

    @Transactional
    public QaInspectionResponse cancelInspection(Long id, String performedBy) {
        QaInspection inspection = repository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection not found: " + id));
        
        if (inspection.getStatus() != InspectionStatus.DRAFT) {
            throw new BusinessException("Only DRAFT inspections can be cancelled.");
        }
        String fromStatus = inspection.getStatus().name();
        inspection.setStatus(InspectionStatus.CANCELLED);
        QaInspection saved = repository.save(inspection);
        trackingLogService.log(TrackingReferenceType.INSPECTION, saved.getId(), fromStatus, saved.getStatus().name(), TrackingAction.CLOSED, performedBy, "Inspection cancelled.");
        return mapper.toResponse(saved);
    }

    @Transactional
    public QaInspectionResponse createDraftFromProduction(ProductionItem item) {
        QaInspection inspection = QaInspection.builder()
                .inspectionNumber(numberGenerator.generateInspectionNumber())
                .productionEntry(item.getProductionEntry())
                .productionItem(item)
                .order(item.getProductionEntry().getOrder())
                .orderItem(item.getOrderItem())
                .heatOrderItem(item.getHeatOrderItem())
                .inspectionDate(LocalDate.now())
                .inspectionStage(InspectionStage.AFTER_FETTLING)
                .inspectionType(InspectionType.VISUAL)
                .status(InspectionStatus.DRAFT)
                .result(InspectionResult.PENDING)
                .totalInspected(item.getFettlingQuantity())
                .inspectorName("PENDING")
                .remarks("Auto-generated from Production Entry: " + item.getProductionEntry().getEntryNumber())
                .findings(new ArrayList<>())
                .build();

        QaInspection saved = repository.save(inspection);
        trackingLogService.log(TrackingReferenceType.INSPECTION, saved.getId(), null, saved.getStatus().name(), TrackingAction.CREATED, "SYSTEM", "Auto-created from Production Entry.");
        return mapper.toResponse(repository.findWithDetailsById(saved.getId()).get());
    }

    private void fetchRelatedEntities(QaInspection inspection) {
        if (inspection.getProductionEntry() != null) {
            if (inspection.getProductionEntry().getId() != null) {
                inspection.setProductionEntry(productionEntryRepository.findById(inspection.getProductionEntry().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Production entry not found")));
            } else {
                inspection.setProductionEntry(null);
            }
        }
        if (inspection.getProductionItem() != null) {
            if (inspection.getProductionItem().getId() != null) {
                inspection.setProductionItem(productionItemRepository.findById(inspection.getProductionItem().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Production item not found")));
            } else {
                inspection.setProductionItem(null);
            }
        }
        if (inspection.getOrder() != null) {
            if (inspection.getOrder().getId() != null) {
                inspection.setOrder(orderRepository.findById(inspection.getOrder().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found")));
            } else {
                inspection.setOrder(null);
            }
        }
        if (inspection.getOrderItem() != null) {
            if (inspection.getOrderItem().getId() != null) {
                inspection.setOrderItem(orderItemRepository.findById(inspection.getOrderItem().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order item not found")));
            } else {
                inspection.setOrderItem(null);
            }
        }
        if (inspection.getHeatOrderItem() != null) {
            if (inspection.getHeatOrderItem().getId() != null) {
                inspection.setHeatOrderItem(heatOrderItemRepository.findById(inspection.getHeatOrderItem().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Heat order item not found")));
            } else {
                inspection.setHeatOrderItem(null);
            }
        }
        
        // Also handle findings
        if (inspection.getFindings() != null) {
            for (InspectionFinding finding : inspection.getFindings()) {
                if (finding.getDefect() != null && finding.getDefect().getId() != null) {
                    finding.setDefect(defectCatalogService.getById(finding.getDefect().getId()));
                }
            }
        }
    }
}
