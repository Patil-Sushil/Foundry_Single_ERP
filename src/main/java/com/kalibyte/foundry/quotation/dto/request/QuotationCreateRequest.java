package com.kalibyte.foundry.quotation.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class QuotationCreateRequest {

    private UUID customerId;

    private UUID enquiryId;

    private LocalDate validUntil;

    private String paymentTerms;

    private String deliveryTerms;

    private String deliveryLocation;

    private List<QuotationItemRequest> items;
}
