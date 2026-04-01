package com.kalibyte.foundry.quotation.dto.response;

import com.kalibyte.foundry.quotation.entity.enums.QuotationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class QuotationResponse {
    private UUID id;
    private String quotationNumber;
    private LocalDate quotationDate;
    private LocalDate validUntil;
    private Integer revisionNo;
    private QuotationStatus status;

    // Customer info
    private UUID customerId;
    private String customerName;

    // Enquiry info
    private UUID enquiryId;
    private String enquiryNumber;

    // Amounts
    private BigDecimal subTotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal totalAmount;

    // Terms
    private String paymentTerms;
    private String deliveryTerms;
    private String deliveryLocation;

    // Items
    private List<QuotationItemResponse> items;
}