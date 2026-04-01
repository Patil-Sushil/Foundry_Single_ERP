package com.kalibyte.foundry.reports.account.dto.response.dailycollection;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record DailyCollectionItem(

        LocalDate date,
        BigDecimal totalAmount,
        Long transactionCount,

        BigDecimal cashAmount,
        BigDecimal upiAmount,
        BigDecimal bankTransferAmount,
        BigDecimal chequeAmount,
        BigDecimal cardAmount

) {}
