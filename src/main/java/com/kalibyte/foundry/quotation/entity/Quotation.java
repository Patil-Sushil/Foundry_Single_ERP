package com.kalibyte.foundry.quotation.entity;
import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.enquiry.entity.Enquiry;
import com.kalibyte.foundry.quotation.entity.enums.QuotationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotations")
@Getter
@Setter
public class Quotation extends BaseEntity {

    @Column(name = "quotation_number", nullable = false, unique = true, length = 50)  // ✅ Fix here
    private String quotationNumber;

    @Column(name = "quotation_date")
    private LocalDate quotationDate;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "revision_no")
    private Integer revisionNo = 0;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_id")
    private Enquiry enquiry;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private QuotationStatus status = QuotationStatus.DRAFT;

    @Column(name = "sub_total", precision = 19, scale = 2)
    private BigDecimal subTotal;

    @Column(precision = 19, scale = 2)
    private BigDecimal discount;

    @Column(precision = 19, scale = 2)
    private BigDecimal tax;

    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_terms", length = 500)
    private String paymentTerms;

    @Column(name = "delivery_terms", length = 500)
    private String deliveryTerms;

    @Column(name = "delivery_location", length = 255)
    private String deliveryLocation;

    @OneToMany(
            mappedBy = "quotation",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<QuotationItem> items = new ArrayList<>();

    // Helper methods
    public void addItem(QuotationItem item) {
        items.add(item);
        item.setQuotation(this);
    }

    public void removeItem(QuotationItem item) {
        items.remove(item);
        item.setQuotation(null);
    }
}