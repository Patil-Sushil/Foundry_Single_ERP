package com.kalibyte.foundry.billing.deliveryChallan.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryChallanItemResponse {

    private String castingName;

    private String patternName;

    private Integer quantity;

    private BigDecimal weight;

    private BigDecimal rate;

    private BigDecimal amount;

    // GST per item
    private BigDecimal gstPercentage;
    private BigDecimal gstAmount;
    private BigDecimal totalWithGst;
}