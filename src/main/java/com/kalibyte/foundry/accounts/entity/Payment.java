package com.kalibyte.foundry.accounts.entity;

import com.kalibyte.foundry.accounts.entity.Enums.PaymentMethod;
import com.kalibyte.foundry.accounts.entity.Enums.PaymentStatus;
import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {
    @Column(name = "payment_number", nullable = false, unique = true)
    private String paymentNumber;

    //------------------------------------------------
    // INVOICE
    //------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    //------------------------------------------------
    // CUSTOMER
    //------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    //------------------------------------------------
    // DATA
    //------------------------------------------------

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amountPaid;

    private String referenceNumber;

    private String remarks;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}
