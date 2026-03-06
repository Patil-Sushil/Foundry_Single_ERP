package com.kalibyte.foundry.quotation.mapper;

import com.kalibyte.foundry.quotation.dto.response.QuotationItemResponse;
import com.kalibyte.foundry.quotation.dto.response.QuotationResponse;
import com.kalibyte.foundry.quotation.entity.Quotation;
import com.kalibyte.foundry.quotation.entity.QuotationItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuotationMapper {

    public QuotationResponse toResponse(Quotation quotation) {
        QuotationResponse response = new QuotationResponse();
        response.setId(quotation.getId());
        response.setQuotationNumber(quotation.getQuotationNumber());
        response.setQuotationDate(quotation.getQuotationDate());
        response.setValidUntil(quotation.getValidUntil());
        response.setRevisionNo(quotation.getRevisionNo());
        response.setStatus(quotation.getStatus());

        // Customer
        if (quotation.getCustomer() != null) {
            response.setCustomerId(quotation.getCustomer().getId());
            response.setCustomerName(quotation.getCustomer().getName());
        }

        // Enquiry
        if (quotation.getEnquiry() != null) {
            response.setEnquiryId(quotation.getEnquiry().getId());
            response.setEnquiryNumber(quotation.getEnquiry().getEnquiryNo());
        }

        // Amounts
        response.setSubTotal(quotation.getSubTotal());
        response.setDiscount(quotation.getDiscount());
        response.setTax(quotation.getTax());
        response.setTotalAmount(quotation.getTotalAmount());

        // Terms
        response.setPaymentTerms(quotation.getPaymentTerms());
        response.setDeliveryTerms(quotation.getDeliveryTerms());
        response.setDeliveryLocation(quotation.getDeliveryLocation());

        // Items
        if (quotation.getItems() != null) {
            response.setItems(
                    quotation.getItems().stream()
                            .map(this::toItemResponse)
                            .collect(Collectors.toList())
            );
        }

        return response;
    }

    public QuotationItemResponse toItemResponse(QuotationItem item) {
        QuotationItemResponse response = new QuotationItemResponse();
        response.setId(item.getId());
        response.setPartName(item.getPartName());
        response.setDrawingNumber(item.getDrawingNumber());
        response.setMaterialGrade(item.getMaterialGrade());
        response.setNetWeightKg(item.getNetWeightKg());
        response.setGrossWeightKg(item.getGrossWeightKg());
        response.setPatternStatus(String.valueOf(item.getPatternStatus()));
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setLineTotal(item.getLineTotal());
        return response;
    }

    public List<QuotationResponse> toResponseList(List<Quotation> quotations) {
        return quotations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}