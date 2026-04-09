package com.kalibyte.foundry.billing.creditnote.entity;

import com.kalibyte.foundry.billing.creditnote.entity.enums.CreditNoteStatus;
import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.enums.GstType;
import com.kalibyte.foundry.qa.customerreturn.entity.CustomerReturn;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "credit_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditNote extends BaseEntity {

    @Column(name = "credit_note_number", nullable = false, unique = true)
    private String creditNoteNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "original_invoice_number")
    private String originalInvoiceNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_return_id")
    private CustomerReturn customerReturn;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    private String reason;

    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "gst_type")
    private GstType gstType;

    @Column(name = "gst_percentage")
    private BigDecimal gstPercentage;

    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal totalGst;
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private CreditNoteStatus status;
}
