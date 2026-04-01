package com.kalibyte.foundry.reports.account.dto.response.aging;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents receivable aging data for a single customer.
 * Each field represents the outstanding amount in a specific aging bucket.
 */
@Builder
public record AgingCustomerItem(

        UUID customerId,

        String customerName,

        BigDecimal current,

        BigDecimal days1to30,

        BigDecimal days31to60,

        BigDecimal days61to90,

        BigDecimal days90plus,

        BigDecimal total
) {}