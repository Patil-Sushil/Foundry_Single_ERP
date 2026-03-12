package com.kalibyte.foundry.quotation.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.util.SecurityUtils;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.enquiry.entity.enums.EnquiryStatus;
import com.kalibyte.foundry.enquiry.entity.Enquiry;
import com.kalibyte.foundry.enquiry.repository.EnquiryRepository;
import com.kalibyte.foundry.quotation.dto.request.QuotationCreateRequest;
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
    private final QuotationMapper quotationMapper;
    private final QuotationEmailService quotationEmailService;

    // ================= CREATE =================

    @Override
    public Quotation create(QuotationCreateRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Quotation must contain at least one item");
        }

        if (request.getEnquiryId() != null &&
                quotationRepository.existsByEnquiryId(request.getEnquiryId())) {
            throw new IllegalStateException("Quotation already exists for this enquiry");
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

        // Link enquiry if provided
        if (request.getEnquiryId() != null) {

            Enquiry enquiry = enquiryRepository.findById(request.getEnquiryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));

            quotation.setEnquiry(enquiry);
        }

        // Add quotation items
        request.getItems().forEach(itemReq -> {

            QuotationItem item = new QuotationItem();
            item.setPartName(itemReq.getPartName());
            item.setDrawingNumber(itemReq.getDrawingNumber());
            item.setMaterialGrade(itemReq.getMaterialGrade());
            item.setNetWeightKg(itemReq.getNetWeightKg());
            item.setGrossWeightKg(itemReq.getGrossWeightKg());
            item.setPatternStatus(itemReq.getPatternStatus());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());

            item.calculateLineTotal();

            quotation.addItem(item);
        });

        recalculateTotals(quotation);

        // Save quotation first
        Quotation savedQuotation = quotationRepository.save(quotation);

        // Update enquiry status if quotation created from enquiry
        if (savedQuotation.getEnquiry() != null) {

            Enquiry enquiry = savedQuotation.getEnquiry();

            if (enquiry.getStatus() != EnquiryStatus.QUOTED) {
                enquiry.setStatus(EnquiryStatus.QUOTED);
            }
        }

        return savedQuotation;
    }

    // ================= GET =================

    @Override
    public Quotation get(UUID id) {
        return quotationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Quotation not found with id: " + id));
    }

    // ================= LIST =================

    @Override
    public PageResponse<QuotationResponse> list(Pageable pageable) {

        Page<Quotation> page = quotationRepository.findAll(pageable);

        return PageResponse.from(page, quotationMapper::toResponse);
    }

    // ================= UPDATE =================

    @Override
    public Quotation update(UUID id, QuotationCreateRequest request) {

        Quotation quotation = get(id);

        if (quotation.getStatus() == QuotationStatus.APPROVED) {
            throw new IllegalStateException("Approved quotation cannot be modified");
        }

        if (quotation.getStatus() == QuotationStatus.SENT) {
            quotation.setStatus(QuotationStatus.DRAFT);
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

        // Clear existing items
        quotation.getItems().clear();

        request.getItems().forEach(itemReq -> {

            QuotationItem item = new QuotationItem();
            item.setPartName(itemReq.getPartName());
            item.setDrawingNumber(itemReq.getDrawingNumber());
            item.setMaterialGrade(itemReq.getMaterialGrade());
            item.setNetWeightKg(itemReq.getNetWeightKg());
            item.setGrossWeightKg(itemReq.getGrossWeightKg());
            item.setPatternStatus(itemReq.getPatternStatus());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());

            item.calculateLineTotal();

            quotation.addItem(item);
        });

        quotation.setRevisionNo(quotation.getRevisionNo() + 1);

        recalculateTotals(quotation);

        return quotationRepository.save(quotation);
    }

    // ================= STATUS =================

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

    // ================= EMAIL =================

    @Override
    public Quotation sendByEmail(UUID id) {

        Quotation quotation = get(id);

        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT quotation can be sent");
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

    // ================= HELPER =================

    private void recalculateTotals(Quotation quotation) {

        BigDecimal subTotal = quotation.getItems().stream()
                .map(QuotationItem::getLineTotal)
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

        int nextSequence = 1;

        if (lastNumber != null) {
            String[] parts = lastNumber.split("-");
            nextSequence = Integer.parseInt(parts[2]) + 1;
        }

        return String.format("%s%05d", prefix, nextSequence);
    }
}