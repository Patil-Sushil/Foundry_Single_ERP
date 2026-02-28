package com.kalibyte.foundry.order.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class QuotationSummary {

    private UUID id;
    private String quotationNumber;
    private LocalDate quotationDate;
    private BigDecimal totalAmount;
}