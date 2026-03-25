package com.kalibyte.foundry.quotation.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.util.SecurityUtils;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.enquiry.entity.Enquiry;
import com.kalibyte.foundry.enquiry.entity.EnquiryItem;
import com.kalibyte.foundry.enquiry.entity.enums.EnquiryStatus;
import com.kalibyte.foundry.enquiry.entity.enums.MetalType;
import com.kalibyte.foundry.enquiry.entity.enums.PatternProvidedBy;
import com.kalibyte.foundry.enquiry.repository.EnquiryRepository;
import com.kalibyte.foundry.pattern.dto.request.PatternReceiptRequest;
import com.kalibyte.foundry.pattern.entity.Pattern;
import com.kalibyte.foundry.pattern.entity.PatternReceipt;
import com.kalibyte.foundry.pattern.repository.PatternRepository;
import com.kalibyte.foundry.quotation.dto.request.QuotationCreateRequest;
import com.kalibyte.foundry.quotation.dto.request.QuotationItemRequest;
import com.kalibyte.foundry.quotation.dto.response.QuotationResponse;
import com.kalibyte.foundry.quotation.entity.Quotation;
import com.kalibyte.foundry.quotation.entity.QuotationItem;
import com.kalibyte.foundry.quotation.entity.enums.QuotationStatus;
import com.kalibyte.foundry.quotation.mapper.QuotationMapper;
import com.kalibyte.foundry.quotation.repository.QuotationRepository;
import com.kalibyte.foundry.quotation.service.QuotationEmailService;
import com.kalibyte.foundry.quotation.service.QuotationService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final EnquiryRepository enquiryRepository;
    private final PatternRepository patternRepository;
    private final QuotationMapper quotationMapper;
    private final QuotationEmailService quotationEmailService;

    // =========================================================
    //  CREATE QUOTATION
    // =========================================================

    @Override
    public Quotation create(QuotationCreateRequest request) {

        Enquiry enquiry = null;

        //--------------------------------------------------
        // SCENARIO 1: WITH ENQUIRY
        //--------------------------------------------------
        if (request.getEnquiryId() != null) {

            if (quotationRepository.existsByEnquiryId(request.getEnquiryId())) {
                throw new IllegalStateException("Quotation already exists for this enquiry");
            }

            enquiry = enquiryRepository.findById(request.getEnquiryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));

            if (request.getCustomerId() == null) {
                request.setCustomerId(enquiry.getCustomer().getId());
            }

            if (!enquiry.getCustomer().getId().equals(request.getCustomerId())) {
                throw new IllegalArgumentException("Customer ID does not match enquiry's customer");
            }
        }

        //--------------------------------------------------
        // SCENARIO 2: DIRECT QUOTATION
        //--------------------------------------------------
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required for direct quotation");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Quotation quotation = new Quotation();
        quotation.setQuotationNumber(generateQuotationNumber());
        quotation.setQuotationDate(LocalDate.now());
        quotation.setValidUntil(request.getValidUntil());
        quotation.setCustomer(customer);
        quotation.setPaymentTerms(request.getPaymentTerms());
        quotation.setDeliveryTerms(request.getDeliveryTerms());
        quotation.setDeliveryLocation(request.getDeliveryLocation());
        quotation.setCreatedBy(SecurityUtils.getCurrentUsername());
        quotation.setStatus(QuotationStatus.DRAFT);

        if (enquiry != null) {
            quotation.setEnquiry(enquiry);
        }

        //--------------------------------------------------
        // PROCESS ITEMS
        //--------------------------------------------------
        processItems(quotation, enquiry, request.getItems());

        //--------------------------------------------------
        // TOTAL CALCULATION
        //--------------------------------------------------
        recalculateTotals(quotation);

        Quotation saved = quotationRepository.save(quotation);

        //--------------------------------------------------
        // UPDATE ENQUIRY STATUS
        //--------------------------------------------------
        if (saved.getEnquiry() != null &&
                saved.getEnquiry().getStatus() != EnquiryStatus.QUOTED) {
            saved.getEnquiry().setStatus(EnquiryStatus.QUOTED);
        }

        return saved;
    }

    // =========================================================
    //  PROCESS ITEMS
    // =========================================================

    private void processItems(Quotation quotation,
                              Enquiry enquiry,
                              List<QuotationItemRequest> itemRequests) {

        //--------------------------------------------------
        // WITH ENQUIRY: Auto-populate from enquiry items
        //--------------------------------------------------
        if (enquiry != null) {

            List<EnquiryItem> enquiryItems = enquiry.getEnquiryItems();

            if (enquiryItems == null || enquiryItems.isEmpty()) {
                throw new IllegalArgumentException("Enquiry has no items");
            }

            for (int i = 0; i < enquiryItems.size(); i++) {

                EnquiryItem enquiryItem = enquiryItems.get(i);

                QuotationItemRequest itemReq = null;
                if (itemRequests != null && i < itemRequests.size()) {
                    itemReq = itemRequests.get(i);
                }

                QuotationItem item = createItemFromEnquiry(enquiryItem, itemReq);
                applyPatternLogic(item, enquiryItem.getPatternProvidedBy(), itemReq);

                item.calculateLineTotal();
                quotation.addItem(item);
            }

            //--------------------------------------------------
            // DIRECT QUOTATION: All fields from request
            //--------------------------------------------------
        } else {

            if (itemRequests == null || itemRequests.isEmpty()) {
                throw new IllegalArgumentException("Items are required for direct quotation");
            }

            for (QuotationItemRequest itemReq : itemRequests) {

                validateDirectQuotationItem(itemReq);

                QuotationItem item = createItemFromRequest(itemReq);
                applyPatternLogic(item, itemReq.getPatternProvidedBy(), itemReq);

                item.calculateLineTotal();
                quotation.addItem(item);
            }
        }
    }

    // =========================================================
    //  CREATE ITEM FROM ENQUIRY (Auto-populate)
    // =========================================================

    private QuotationItem createItemFromEnquiry(EnquiryItem enquiryItem,
                                                QuotationItemRequest itemReq) {

        QuotationItem item = new QuotationItem();

        // ===== AUTO-POPULATED FROM ENQUIRY =====
        item.setPartName(enquiryItem.getPartName());
        item.setMaterialGrade(enquiryItem.getMaterialGrade());
        item.setMetalType(enquiryItem.getMetalType());
        item.setCastingProcess(enquiryItem.getCastingProcess());
        item.setQuantity(enquiryItem.getRequiredQuantity());
        item.setNetWeightKg(enquiryItem.getApproxPieceWeightKg());
        item.setGrossWeightKg(enquiryItem.getApproxPieceWeightKg());

        // ===== OVERRIDE WITH REQUEST IF PROVIDED =====
        if (itemReq != null) {
            if (itemReq.getPartName() != null && !itemReq.getPartName().isBlank()) {
                item.setPartName(itemReq.getPartName());
            }
            if (itemReq.getMaterialGrade() != null && !itemReq.getMaterialGrade().isBlank()) {
                item.setMaterialGrade(itemReq.getMaterialGrade());
            }
            if (itemReq.getMetalType() != null) {
                item.setMetalType(itemReq.getMetalType());
            }
            if (itemReq.getCastingProcess() != null && !itemReq.getCastingProcess().isBlank()) {
                item.setCastingProcess(itemReq.getCastingProcess());
            }
            if (itemReq.getQuantity() != null && itemReq.getQuantity() > 0) {
                item.setQuantity(itemReq.getQuantity());
            }
            if (itemReq.getNetWeightKg() != null) {
                item.setNetWeightKg(itemReq.getNetWeightKg());
            }
            if (itemReq.getGrossWeightKg() != null) {
                item.setGrossWeightKg(itemReq.getGrossWeightKg());
            }
            if (itemReq.getDrawingNumber() != null) {
                item.setDrawingNumber(itemReq.getDrawingNumber());
            }
            if (itemReq.getPatternStatus() != null) {
                item.setPatternStatus(itemReq.getPatternStatus());
            }
            if (itemReq.getUnitPrice() != null) {
                item.setUnitPrice(itemReq.getUnitPrice());
            }
        }

        if (item.getUnitPrice() == null) {
            item.setUnitPrice(BigDecimal.ZERO);
        }

        return item;
    }

    // =========================================================
    //  CREATE ITEM FROM REQUEST (Direct Quotation)
    // =========================================================

    private QuotationItem createItemFromRequest(QuotationItemRequest itemReq) {

        QuotationItem item = new QuotationItem();

        item.setPartName(itemReq.getPartName());
        item.setMaterialGrade(itemReq.getMaterialGrade());
        item.setMetalType(itemReq.getMetalType());
        item.setCastingProcess(itemReq.getCastingProcess());
        item.setDrawingNumber(itemReq.getDrawingNumber());
        item.setNetWeightKg(itemReq.getNetWeightKg());
        item.setGrossWeightKg(itemReq.getGrossWeightKg());
        item.setPatternStatus(itemReq.getPatternStatus());
        item.setQuantity(itemReq.getQuantity());
        item.setUnitPrice(itemReq.getUnitPrice());

        return item;
    }

    // =========================================================
    //  VALIDATE DIRECT QUOTATION ITEM
    // =========================================================

    private void validateDirectQuotationItem(QuotationItemRequest itemReq) {

        if (itemReq.getPartName() == null || itemReq.getPartName().isBlank()) {
            throw new IllegalArgumentException("Part name is required");
        }
        if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (itemReq.getUnitPrice() == null) {
            throw new IllegalArgumentException("Unit price is required");
        }
        if (itemReq.getPatternProvidedBy() == null) {
            throw new IllegalArgumentException("Pattern source is required for direct quotation");
        }
    }

    // =========================================================
    //  PATTERN LOGIC
    // =========================================================

    private void applyPatternLogic(QuotationItem item,
                                   PatternProvidedBy source,
                                   QuotationItemRequest itemReq) {

        if (source == null) {
            throw new IllegalArgumentException("Pattern source cannot be null");
        }

        if (source == PatternProvidedBy.CUSTOMER) {

            item.setPatternProvidedByCustomer(true);

            if (itemReq != null && itemReq.getPatternReceipt() != null) {
                PatternReceiptRequest pr = itemReq.getPatternReceipt();
                PatternReceipt receipt = PatternReceipt.builder()
                        .name(pr.getName())
                        .type(pr.getType())
                        .material(pr.getMaterial())
                        .inwardDate(pr.getInwardDate())
                        .outwardDate(pr.getOutwardDate())
                        .build();
                item.setPatternReceipt(receipt);
            } else if (itemReq != null) {
                throw new IllegalArgumentException("Pattern receipt is required for customer pattern");
            }

        } else {

            item.setPatternProvidedByCustomer(false);

            if (itemReq != null && itemReq.getPatternId() != null) {
                Pattern pattern = patternRepository.findById(itemReq.getPatternId())
                        .orElseThrow(() -> new ResourceNotFoundException("Pattern not found"));
                item.setPattern(pattern);
            } else if (itemReq != null) {
                throw new IllegalArgumentException("Pattern ID is required for company pattern");
            }
        }
    }

    // =========================================================
    //  GET / LIST / UPDATE / SEND
    // =========================================================

    @Override
    @Transactional
    public Quotation get(UUID id) {
        return quotationRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
    }

    @Override
    public PageResponse<QuotationResponse> list(Pageable pageable) {
        Page<Quotation> page = quotationRepository.findAll(pageable);
        return PageResponse.from(page, quotationMapper::toResponse);
    }

    @Override
    public Quotation update(UUID id, QuotationCreateRequest request) {

        Quotation quotation = get(id);

        if (quotation.getStatus() == QuotationStatus.APPROVED) {
            throw new IllegalStateException("Approved quotation cannot be modified");
        }

        if (quotation.getStatus() == QuotationStatus.SENT) {
            quotation.setStatus(QuotationStatus.REVISED);
            quotation.setSentAt(null);
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        quotation.setCustomer(customer);
        quotation.setValidUntil(request.getValidUntil());
        quotation.setPaymentTerms(request.getPaymentTerms());
        quotation.setDeliveryTerms(request.getDeliveryTerms());
        quotation.setDeliveryLocation(request.getDeliveryLocation());
        quotation.setUpdatedBy(SecurityUtils.getCurrentUsername());

        quotation.clearItems();

        Enquiry enquiry = null;
        if (request.getEnquiryId() != null) {
            enquiry = enquiryRepository.findById(request.getEnquiryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));
        }

        processItems(quotation, enquiry, request.getItems());

        quotation.setRevisionNo(quotation.getRevisionNo() + 1);
        recalculateTotals(quotation);

        return quotationRepository.save(quotation);
    }

    @Override
    public Quotation updateStatus(UUID id, QuotationStatus newStatus) {

        Quotation quotation = get(id);
        validateStatusTransition(quotation.getStatus(), newStatus);

        quotation.setStatus(newStatus);
        quotation.setUpdatedBy(SecurityUtils.getCurrentUsername());

        switch (newStatus) {
            case SENT -> quotation.setSentAt(LocalDateTime.now());
            case APPROVED -> quotation.setApprovedAt(LocalDateTime.now());
            case CANCELLED -> quotation.setRejectedAt(LocalDateTime.now());
        }

        return quotationRepository.save(quotation);
    }

    @Override
    public Quotation sendByEmail(UUID id) {

        Quotation quotation = get(id);

        if (quotation.getStatus() != QuotationStatus.DRAFT &&
                quotation.getStatus() != QuotationStatus.REVISED) {
            throw new IllegalStateException("Only DRAFT and REVISED quotations can be sent");
        }

        if (quotation.getCustomer().getEmail() == null) {
            throw new IllegalStateException("Customer email not available");
        }

        quotationEmailService.sendQuotationEmail(quotation);

        quotation.setStatus(QuotationStatus.SENT);
        quotation.setSentAt(LocalDateTime.now());
        quotation.setUpdatedBy(SecurityUtils.getCurrentUsername());

        return quotationRepository.save(quotation);
    }

    // =========================================================
    //  HELPERS
    // =========================================================

    private void recalculateTotals(Quotation quotation) {
        BigDecimal subTotal = quotation.getItems().stream()
                .map(QuotationItem::getLineTotal)
                .filter(lt -> lt != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        quotation.setSubTotal(subTotal);
        quotation.setTotalAmount(subTotal);
    }

    private synchronized String generateQuotationNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "QUO-" + year + "-";
        String lastNumber = quotationRepository
                .findTopByQuotationNumberStartingWithOrderByQuotationNumberDesc(prefix)
                .map(Quotation::getQuotationNumber)
                .orElse(null);
        int next = 1;
        if (lastNumber != null) {
            next = Integer.parseInt(lastNumber.split("-")[2]) + 1;
        }
        return String.format("%s%04d", prefix, next);
    }

    private void validateStatusTransition(QuotationStatus current, QuotationStatus next) {
        if (current == QuotationStatus.APPROVED) {
            throw new IllegalStateException("Approved quotation cannot be modified");
        }
        if (current == QuotationStatus.DRAFT && next == QuotationStatus.APPROVED) {
            throw new IllegalStateException("Draft quotation must be sent before approval");
        }
        if (current == QuotationStatus.SENT &&
                !(next == QuotationStatus.APPROVED || next == QuotationStatus.CANCELLED)) {
            throw new IllegalStateException("Sent quotation can only be Approved or Rejected");
        }
    }
}