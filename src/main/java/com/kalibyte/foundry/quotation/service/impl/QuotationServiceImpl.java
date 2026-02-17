package com.kalibyte.foundry.quotation.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.util.SecurityUtils;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.enquiry.repository.EnquiryRepository;
import com.kalibyte.foundry.quotation.dto.request.QuotationCreateRequest;
import com.kalibyte.foundry.quotation.dto.response.QuotationResponse;
import com.kalibyte.foundry.quotation.entity.Quotation;
import com.kalibyte.foundry.quotation.entity.QuotationItem;
import com.kalibyte.foundry.quotation.entity.enums.QuotationStatus;
import com.kalibyte.foundry.quotation.mapper.QuotationMapper;
import com.kalibyte.foundry.quotation.repository.QuotationRepository;
import com.kalibyte.foundry.quotation.service.QuotationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final EnquiryRepository enquiryRepository;
    private final QuotationMapper quotationMapper;

    // ========================= CREATE =========================

    @Override
    public Quotation create(QuotationCreateRequest request) {

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

        if (request.getEnquiryId() != null) {
            quotation.setEnquiry(
                    enquiryRepository.findById(request.getEnquiryId())
                            .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"))
            );
        }

        request.getItems().forEach(itemReq -> {
            QuotationItem item = new QuotationItem();
            item.setQuotation(quotation);
            item.setPartName(itemReq.getPartName());
            item.setDrawingNumber(itemReq.getDrawingNumber());
            item.setMaterialGrade(itemReq.getMaterialGrade());
            item.setNetWeightKg(itemReq.getNetWeightKg());
            item.setGrossWeightKg(itemReq.getGrossWeightKg());
            item.setPatternStatus(itemReq.getPatternStatus());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());

            BigDecimal lineTotal = itemReq.getUnitPrice()
                    .multiply(itemReq.getQuantity());

            item.setLineTotal(lineTotal);
            quotation.getItems().add(item);
        });

        recalculateTotals(quotation);

        return quotationRepository.save(quotation);
    }

    // ========================= GET =========================

    @Override
    public Quotation get(UUID id) {
        return quotationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Quotation not found with id: " + id));
    }

    // ========================= LIST =========================

    @Override
    public PageResponse<QuotationResponse> list(Pageable pageable) {

        Page<Quotation> page = quotationRepository.findAll(pageable);

        return PageResponse.from(page, quotationMapper::toResponse);
    }

    // ========================= UPDATE =========================

    @Override
    public Quotation update(UUID id, QuotationCreateRequest request) {

        Quotation quotation = get(id);

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        quotation.setCustomer(customer);
        quotation.setValidUntil(request.getValidUntil());
        quotation.setPaymentTerms(request.getPaymentTerms());
        quotation.setDeliveryTerms(request.getDeliveryTerms());
        quotation.setDeliveryLocation(request.getDeliveryLocation());
        quotation.setUpdatedBy(SecurityUtils.getCurrentUsername());

        quotation.getItems().clear();

        request.getItems().forEach(itemReq -> {
            QuotationItem item = new QuotationItem();
            item.setQuotation(quotation);
            item.setPartName(itemReq.getPartName());
            item.setDrawingNumber(itemReq.getDrawingNumber());
            item.setMaterialGrade(itemReq.getMaterialGrade());
            item.setNetWeightKg(itemReq.getNetWeightKg());
            item.setGrossWeightKg(itemReq.getGrossWeightKg());
            item.setPatternStatus(itemReq.getPatternStatus());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());

            BigDecimal lineTotal =
                    itemReq.getUnitPrice().multiply(itemReq.getQuantity());

            item.setLineTotal(lineTotal);
            quotation.getItems().add(item);
        });

        quotation.setRevisionNo(quotation.getRevisionNo() + 1);

        recalculateTotals(quotation);

        return quotationRepository.save(quotation);
    }

    // ========================= STATUS =========================

    @Override
    public Quotation updateStatus(UUID id, QuotationStatus status) {

        Quotation quotation = get(id);
        quotation.setStatus(status);
        quotation.setUpdatedBy(SecurityUtils.getCurrentUsername());

        return quotationRepository.save(quotation);
    }

    // ========================= HELPER =========================

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
