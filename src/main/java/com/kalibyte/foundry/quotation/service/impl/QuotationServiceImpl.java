package com.kalibyte.foundry.quotation.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.util.SecurityUtils;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.enquiry.entity.Enquiry;
import com.kalibyte.foundry.enquiry.entity.EnquiryItem;
import com.kalibyte.foundry.enquiry.entity.enums.EnquiryStatus;
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

    //--------------------------------------------------
    // CREATE QUOTATION
    //--------------------------------------------------
    @Override
    public Quotation create(QuotationCreateRequest request) {

        // 1. Fetch customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // 2. Create quotation
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

        //--------------------------------------------------
        // 3. HANDLE ENQUIRY (OPTIONAL)
        //--------------------------------------------------
        Enquiry enquiry = null;

        if (request.getEnquiryId() != null) {

            if (quotationRepository.existsByEnquiryId(request.getEnquiryId())) {
                throw new IllegalStateException("Quotation already exists for this enquiry");
            }

            enquiry = enquiryRepository.findById(request.getEnquiryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));

            quotation.setEnquiry(enquiry);
        }

        //--------------------------------------------------
        // 4. VALIDATE ITEMS
        //--------------------------------------------------
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Quotation must contain at least one item");
        }

        //--------------------------------------------------
        // 5. PROCESS ITEMS
        //--------------------------------------------------
        int index = 0;

        for (QuotationItemRequest itemReq : request.getItems()) {

            QuotationItem item = new QuotationItem();

            // Basic fields
            item.setPartName(itemReq.getPartName());
            item.setDrawingNumber(itemReq.getDrawingNumber());
            item.setMaterialGrade(itemReq.getMaterialGrade());
            item.setNetWeightKg(itemReq.getNetWeightKg());
            item.setGrossWeightKg(itemReq.getGrossWeightKg());
            item.setPatternStatus(itemReq.getPatternStatus());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());

            //--------------------------------------------------
            //  PATTERN LOGIC (CORE)
            //--------------------------------------------------
            applyPatternLogic(item, itemReq, enquiry, index);

            item.calculateLineTotal();
            quotation.addItem(item);

            index++;
        }

        //--------------------------------------------------
        // 6. TOTAL CALCULATION
        //--------------------------------------------------
        recalculateTotals(quotation);

        Quotation saved = quotationRepository.save(quotation);

        //--------------------------------------------------
        // 7. UPDATE ENQUIRY STATUS
        //--------------------------------------------------
        if (saved.getEnquiry() != null &&
                saved.getEnquiry().getStatus() != EnquiryStatus.QUOTED) {
            saved.getEnquiry().setStatus(EnquiryStatus.QUOTED);
        }

        return saved;
    }

    //--------------------------------------------------
    //  PATTERN LOGIC (CORE METHOD)
    //--------------------------------------------------
    private void applyPatternLogic(QuotationItem item,
                                   QuotationItemRequest req,
                                   Enquiry enquiry,
                                   int index) {

        PatternProvidedBy source;

        //--------------------------------------------------
        // CASE 1: FROM ENQUIRY
        //--------------------------------------------------
        if (enquiry != null) {

            EnquiryItem enquiryItem = enquiry.getEnquiryItems().get(index);
            source = enquiryItem.getPatternProvidedBy();

            // Validate mismatch
            if (req.getPatternProvidedBy() != null &&
                    req.getPatternProvidedBy() != source) {
                throw new IllegalArgumentException("Pattern source mismatch with enquiry");
            }

            //--------------------------------------------------
            // CASE 2: DIRECT QUOTATION
            //--------------------------------------------------
        } else {

            if (req.getPatternProvidedBy() == null) {
                throw new IllegalArgumentException("patternProvidedBy is required");
            }

            source = req.getPatternProvidedBy();
        }

        //--------------------------------------------------
        // APPLY PATTERN LOGIC
        //--------------------------------------------------

        // CUSTOMER PATTERN
        if (source == PatternProvidedBy.CUSTOMER) {

            if (req.getPatternReceipt() == null) {
                throw new IllegalArgumentException("Pattern receipt required");
            }

            PatternReceiptRequest pr = req.getPatternReceipt();

            PatternReceipt receipt = PatternReceipt.builder()
                    .name(pr.getName())
                    .type(pr.getType())
                    .material(pr.getMaterial())
                    .inwardDate(pr.getInwardDate())
                    .outwardDate(pr.getOutwardDate())
                    .build();

            item.setPatternReceipt(receipt);
            item.setPatternProvidedByCustomer(true);

            // COMPANY PATTERN
        } else {

            if (req.getPatternId() == null) {
                throw new IllegalArgumentException("Pattern ID required");
            }

            Pattern pattern = patternRepository.findById(req.getPatternId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pattern not found"));

            item.setPattern(pattern);
            item.setPatternProvidedByCustomer(false);
        }
    }

    //--------------------------------------------------
    // GET
    //--------------------------------------------------
    @Override
    @Transactional
    public Quotation get(UUID id) {
        return quotationRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
    }

    //--------------------------------------------------
    // LIST
    //--------------------------------------------------
    @Override
    public PageResponse<QuotationResponse> list(Pageable pageable) {
        Page<Quotation> page = quotationRepository.findAll(pageable);
        return PageResponse.from(page, quotationMapper::toResponse);
    }

    //--------------------------------------------------
    // UPDATE
    //--------------------------------------------------
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

        int index = 0;

        for (QuotationItemRequest itemReq : request.getItems()) {

            QuotationItem item = new QuotationItem();

            item.setPartName(itemReq.getPartName());
            item.setDrawingNumber(itemReq.getDrawingNumber());
            item.setMaterialGrade(itemReq.getMaterialGrade());
            item.setNetWeightKg(itemReq.getNetWeightKg());
            item.setGrossWeightKg(itemReq.getGrossWeightKg());
            item.setPatternStatus(itemReq.getPatternStatus());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());

            applyPatternLogic(item, itemReq, enquiry, index);

            item.calculateLineTotal();
            quotation.addItem(item);

            index++;
        }

        quotation.setRevisionNo(quotation.getRevisionNo() + 1);

        recalculateTotals(quotation);

        return quotationRepository.save(quotation);
    }

    //--------------------------------------------------
    // STATUS UPDATE
    //--------------------------------------------------
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

    //--------------------------------------------------
    // SEND EMAIL
    //--------------------------------------------------
    @Override
    public Quotation sendByEmail(UUID id) {

        Quotation quotation = get(id);

        if (quotation.getStatus() != QuotationStatus.DRAFT && quotation.getStatus() != QuotationStatus.REVISED) {
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

    //--------------------------------------------------
    // TOTAL CALCULATION
    //--------------------------------------------------
    private void recalculateTotals(Quotation quotation) {

        BigDecimal subTotal = quotation.getItems().stream()
                .map(QuotationItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        quotation.setSubTotal(subTotal);
        quotation.setTotalAmount(subTotal);
    }

    //--------------------------------------------------
    // NUMBER GENERATION
    //--------------------------------------------------
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