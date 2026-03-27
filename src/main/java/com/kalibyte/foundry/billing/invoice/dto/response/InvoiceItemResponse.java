package com.kalibyte.foundry.billing.invoice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItemResponse {

    private UUID id;
    private String partName;
    private String materialGrade;

    private Integer quantity;
    private BigDecimal weight;
    private BigDecimal rate;
    private BigDecimal amount;

    // GST per item
    private BigDecimal gstPercentage;
    private BigDecimal gstAmount;
    private BigDecimal totalWithGst;
}