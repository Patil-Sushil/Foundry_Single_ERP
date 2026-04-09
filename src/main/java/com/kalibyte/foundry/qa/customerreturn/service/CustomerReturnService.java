package com.kalibyte.foundry.qa.customerreturn.service;

import com.kalibyte.foundry.billing.creditnote.service.CreditNoteService;
import com.kalibyte.foundry.order.dto.request.OrderCreateRequest;
import com.kalibyte.foundry.order.dto.request.OrderItemRequest;
import com.kalibyte.foundry.order.service.OrderService;
import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.order.repository.OrderItemRepository;
import com.kalibyte.foundry.qa.common.QaNumberGenerator;
import com.kalibyte.foundry.qa.common.enums.*;
import com.kalibyte.foundry.qa.customerreturn.entity.CustomerReturn;
import com.kalibyte.foundry.qa.customerreturn.repository.CustomerReturnRepository;
import com.kalibyte.foundry.qa.customerreturn.repository.CustomerReturnSpecification;
import com.kalibyte.foundry.qa.tracking.service.QaTrackingLogService;
import com.kalibyte.foundry.scrap.dto.request.ScrapEntryRequest;
import com.kalibyte.foundry.scrap.dto.request.ScrapItemRequest;
import com.kalibyte.foundry.scrap.enums.ScrapSource;
import com.kalibyte.foundry.scrap.enums.ScrapStatus;
import com.kalibyte.foundry.scrap.service.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerReturnService {

    private final CustomerReturnRepository repository;
    private final ScrapService scrapService;
    private final QaTrackingLogService trackingLogService;
    private final QaNumberGenerator numberGenerator;
    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;
    private final CreditNoteService creditNoteService;

    @Transactional(readOnly = true)
    public List<CustomerReturn> list(LocalDate startDate, LocalDate endDate, UUID customerId, UUID orderId, ReturnStatus status, ReturnDisposition disposition) {
        Specification<CustomerReturn> spec = CustomerReturnSpecification.withFilters(startDate, endDate, customerId, orderId, status, disposition);
        return repository.findAll(spec);
    }

    @Transactional(readOnly = true)
    public CustomerReturn getById(Long id) {
        return repository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer return not found: " + id));
    }

    @Transactional
    public CustomerReturn receiveReturn(CustomerReturn returnEntry) {
        returnEntry.setReturnNumber(numberGenerator.generateReturnNumber());
        returnEntry.setStatus(ReturnStatus.RECEIVED);
        returnEntry.setDisposition(ReturnDisposition.PENDING_ASSESSMENT);
        
        // Ensure unit weight and material grade are fetched from OrderItem
        if (returnEntry.getOrderItem() != null && returnEntry.getOrderItem().getId() != null) {
            var orderItem = orderItemRepository.findById(returnEntry.getOrderItem().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
            returnEntry.setOrderItem(orderItem);
            returnEntry.setMaterialGrade(orderItem.getMaterialGrade());
            if (orderItem.getNetWeightKg() != null) {
                returnEntry.setReturnedWeight(orderItem.getNetWeightKg().multiply(BigDecimal.valueOf(returnEntry.getReturnedQuantity())));
            }
        }

        CustomerReturn saved = repository.save(returnEntry);
        trackingLogService.log(TrackingReferenceType.CUSTOMER_RETURN, saved.getId(), null, saved.getStatus().name(), TrackingAction.CREATED, "SYSTEM", "Return received.");
        return saved;
    }

    @Transactional
    public CustomerReturn assessReturn(Long id, QaFinding finding, RootCauseCategory rootCause, String rootCauseDesc, String inspectorName, String remarks) {
        CustomerReturn existing = getById(id);
        if (existing.getStatus() != ReturnStatus.RECEIVED && existing.getStatus() != ReturnStatus.UNDER_ASSESSMENT) {
            throw new BusinessException("Return is already assessed or closed.");
        }

        String fromStatus = existing.getStatus().name();
        existing.setQaFinding(finding);
        existing.setRootCauseCategory(rootCause);
        existing.setRootCauseDescription(rootCauseDesc);
        existing.setQaInspectorName(inspectorName);
        existing.setQaRemarks(remarks);
        existing.setQaAssessmentDate(LocalDate.now());
        existing.setStatus(ReturnStatus.ASSESSED);

        CustomerReturn saved = repository.save(existing);
        trackingLogService.log(TrackingReferenceType.CUSTOMER_RETURN, saved.getId(), fromStatus, saved.getStatus().name(), TrackingAction.INSPECTED, inspectorName, "Assessment completed.");
        return saved;
    }

    @Transactional
    public CustomerReturn dispositionReturn(Long id, ReturnDisposition disposition, String remarks, String performedBy, BigDecimal creditAmount, UUID replacementOrderId) {
        CustomerReturn existing = getById(id);
        if (existing.getStatus() != ReturnStatus.ASSESSED) {
            throw new BusinessException("Return must be assessed before disposition.");
        }

        String fromStatus = existing.getStatus().name();
        existing.setDisposition(disposition);
        existing.setDispositionDate(LocalDate.now());
        existing.setDispositionBy(performedBy);
        existing.setQaRemarks(existing.getQaRemarks() + "\nDisposition Remarks: " + remarks);
        
        if (disposition == ReturnDisposition.CREDIT_NOTE) {
            existing.setCreditAmount(creditAmount);
            createCreditNoteFromReturn(existing, creditAmount);
        } else if (disposition == ReturnDisposition.REPLACE) {
            if (replacementOrderId != null) {
                existing.setReplacementOrderId(replacementOrderId);
            } else {
                createReplacementOrderFromReturn(existing);
            }
        } else if (disposition == ReturnDisposition.SCRAP_FOR_REMELT || disposition == ReturnDisposition.SCRAP_FOR_SALE) {
            createScrapFromReturn(existing);
        }

        existing.setStatus(ReturnStatus.DISPOSITIONED);
        CustomerReturn saved = repository.save(existing);
        trackingLogService.log(TrackingReferenceType.CUSTOMER_RETURN, saved.getId(), fromStatus, saved.getStatus().name(), TrackingAction.DISPOSITIONED, performedBy, remarks);
        return saved;
    }

    @Transactional
    public CustomerReturn closeReturn(Long id, String performedBy) {
        CustomerReturn existing = getById(id);
        String fromStatus = existing.getStatus().name();
        existing.setStatus(ReturnStatus.CLOSED);
        CustomerReturn saved = repository.save(existing);
        trackingLogService.log(TrackingReferenceType.CUSTOMER_RETURN, saved.getId(), fromStatus, saved.getStatus().name(), TrackingAction.CLOSED, performedBy, "Return closed.");
        return saved;
    }

    private void createScrapFromReturn(CustomerReturn returnEntry) {
        ScrapEntryRequest scrapRequest = ScrapEntryRequest.builder()
                .scrapSource(ScrapSource.CUSTOMER_RETURN)
                .grade(returnEntry.getMaterialGrade())
                .totalWeight(returnEntry.getReturnedWeight())
                .customerReturnId(returnEntry.getId())
                .returnNumber(returnEntry.getReturnNumber())
                .initialStatus(ScrapStatus.PENDING_VERIFICATION)
                .remarks("Customer Return: " + returnEntry.getReturnNumber() + ". Complaint: " + returnEntry.getComplaintDescription())
                .scrapItems(List.of(ScrapItemRequest.builder()
                        .itemName(returnEntry.getOrderItem().getPartName() + " (Returned)")
                        .grade(returnEntry.getMaterialGrade())
                        .weight(returnEntry.getReturnedWeight())
                        .quantity(returnEntry.getReturnedQuantity())
                        .scrapType("CUSTOMER_RETURN")
                        .recyclability("HIGH")
                        .build()))
                .build();

        var scrapResponse = scrapService.createScrapEntry(scrapRequest);
        returnEntry.setScrapEntryId(scrapResponse.getId());
        trackingLogService.log(TrackingReferenceType.CUSTOMER_RETURN, returnEntry.getId(), returnEntry.getStatus().name(), returnEntry.getStatus().name(), TrackingAction.SCRAP_GENERATED, "SYSTEM", "Scrap entry created: " + scrapResponse.getScrapNumber());
    }

    private void createReplacementOrderFromReturn(CustomerReturn returnEntry) {
        var originalItem = returnEntry.getOrderItem();
        
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .partName(originalItem.getPartName())
                .materialGrade(originalItem.getMaterialGrade())
                .metalType(originalItem.getMetalType())
                .castingProcess(originalItem.getCastingProcess())
                .netWeightKg(originalItem.getNetWeightKg())
                .grossWeightKg(originalItem.getGrossWeightKg())
                .quantity(returnEntry.getReturnedQuantity())
                .unitPrice(originalItem.getUnitPrice())
                .gstPercentage(originalItem.getGstPercentage())
                .patternProvidedByCustomer(originalItem.getPatternProvidedByCustomer())
                .patternId(originalItem.getPattern() != null ? originalItem.getPattern().getId() : null)
                .build();

        OrderCreateRequest orderRequest = OrderCreateRequest.builder()
                .customerId(returnEntry.getCustomer().getId())
                .deliveryDate(LocalDate.now().plusWeeks(2)) // Default 2 weeks for replacement
                .gstPercentage(returnEntry.getOrder().getGstPercentage())
                .placeOfSupply(returnEntry.getOrder().getPlaceOfSupply())
                .poReference("REPLACEMENT-" + returnEntry.getReturnNumber())
                .paymentTerms(returnEntry.getOrder().getPaymentTerms())
                .customPaymentTerms(returnEntry.getOrder().getCustomPaymentTerms())
                .items(List.of(itemRequest))
                .build();

        var orderResponse = orderService.createOrder(orderRequest);
        returnEntry.setReplacementOrderId(orderResponse.getId());
        trackingLogService.log(TrackingReferenceType.CUSTOMER_RETURN, returnEntry.getId(), returnEntry.getStatus().name(), returnEntry.getStatus().name(), TrackingAction.DISPOSITIONED, "SYSTEM", "Replacement order created: " + orderResponse.getOrderNumber());
    }

    private void createCreditNoteFromReturn(CustomerReturn returnEntry, BigDecimal creditAmount) {
        var cnResponse = creditNoteService.generateCreditNoteFromReturn(returnEntry, creditAmount);
        returnEntry.setCreditNoteId(cnResponse.getId());
        trackingLogService.log(TrackingReferenceType.CUSTOMER_RETURN, returnEntry.getId(), returnEntry.getStatus().name(), returnEntry.getStatus().name(), TrackingAction.DISPOSITIONED, "SYSTEM", "Credit note created: " + cnResponse.getCreditNoteNumber());
    }
}
