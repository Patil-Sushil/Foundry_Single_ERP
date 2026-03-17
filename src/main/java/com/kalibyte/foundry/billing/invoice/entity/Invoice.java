package com.kalibyte.foundry.billing.invoice.entity;

import com.kalibyte.foundry.billing.invoice.entity.enums.InvoiceStatus;
import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"order","items"})
public class Invoice extends BaseEntity {

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    //------------------------------------------------
    // ORDER
    //------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    //------------------------------------------------
    // ITEMS
    //------------------------------------------------

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<InvoiceItem> items;

    //------------------------------------------------
    // DATA
    //------------------------------------------------

    private String vehicleNumber;

    private BigDecimal subtotal;

    private BigDecimal cgst;

    private BigDecimal sgst;

    private BigDecimal igst;

    private BigDecimal gstPercentage;

    private BigDecimal totalAmount;

    private LocalDate invoiceDate;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus billStatus;

    public Customer getCustomer(){
        return order.getCustomer();
    }
}