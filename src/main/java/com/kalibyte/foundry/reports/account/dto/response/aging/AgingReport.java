package com.kalibyte.foundry.reports.account.dto.response.aging;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Final Receivables Aging Report response returned to the API layer.
 */
@Builder
public record AgingReport(

        LocalDate asOfDate,

        BigDecimal totalOutstanding,

        AgingSummary summary,

        List<AgingCustomerItem> customers
) {}