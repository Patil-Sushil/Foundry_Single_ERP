package com.kalibyte.foundry.payment.dto.request;

import com.kalibyte.foundry.payment.entity.Enums.PaymentMethod;
import com.kalibyte.foundry.payment.entity.Enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFilterRequest {

    private UUID customerId;
    private UUID invoiceId;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String paymentNumber;

    // ── Pagination with SAFE defaults ──
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDir;

    // ── Safe getters that guarantee non-null defaults ──
    public int getPage() {
        return page != null ? page : 0;
    }

    public int getSize() {
        return size != null ? size : 20;
    }

    public String getSortBy() {
        return sortBy != null && !sortBy.isBlank()
                ? sortBy
                : "paymentDate";
    }

    public String getSortDir() {
        return sortDir != null && !sortDir.isBlank()
                ? sortDir
                : "desc";
    }
}