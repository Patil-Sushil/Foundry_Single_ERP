package com.kalibyte.foundry.order.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.customer.entity.Customer;

import com.kalibyte.foundry.order.entity.enums.GstType;
import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import com.kalibyte.foundry.order.entity.enums.OrderType;
import com.kalibyte.foundry.order.entity.enums.PaymentTerms;
import com.kalibyte.foundry.quotation.entity.Quotation;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String orderNumber;

    //------------------------------------------------
    // CUSTOMER
    //------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    //------------------------------------------------
    // TYPE
    //------------------------------------------------
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    //------------------------------------------------
    // QUOTATION (OPTIONAL)
    //------------------------------------------------
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", unique = true)
    private Quotation quotation;

    //------------------------------------------------
    // DETAILS
    //------------------------------------------------
    private LocalDate orderDate;
    private LocalDate deliveryDate;

    private String placeOfSupply;
    private String poReference;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CREATED;

    //------------------------------------------------
    // PAYMENT TERMS
    //------------------------------------------------
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_terms")
    private PaymentTerms paymentTerms;

    @Column(name = "custom_payment_terms")
    private String customPaymentTerms;

    //------------------------------------------------
    // AMOUNTS
    //------------------------------------------------
    @Builder.Default
    private BigDecimal subTotal = BigDecimal.ZERO;

    //------------------------------------------------
    // GST FIELDS
    //------------------------------------------------
    @Enumerated(EnumType.STRING)
    @Column(name = "gst_type")
    private GstType gstType;

    @Builder.Default
    @Column(name = "gst_percentage", precision = 5, scale = 2)
    private BigDecimal gstPercentage = BigDecimal.valueOf(18);

    @Builder.Default
    @Column(name = "cgst", precision = 19, scale = 2)
    private BigDecimal cgst = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "sgst", precision = 19, scale = 2)
    private BigDecimal sgst = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "igst", precision = 19, scale = 2)
    private BigDecimal igst = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_gst", precision = 19, scale = 2)
    private BigDecimal totalGst = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    //------------------------------------------------
    // ITEMS
    //------------------------------------------------
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    //------------------------------------------------
    // HELPERS
    //------------------------------------------------
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void clearItems() {
        items.forEach(i -> i.setOrder(null));
        items.clear();
    }

    /**
     * Returns the display-friendly payment terms string.
     * If CUSTOM, returns the customPaymentTerms text.
     */
    public String getPaymentTermsDisplay() {
        if (paymentTerms == null) return null;
        if (paymentTerms == PaymentTerms.CUSTOM) {
            return customPaymentTerms != null ? customPaymentTerms : "Custom Terms";
        }
        return paymentTerms.getDisplayName();
    }
}