package com.kalibyte.foundry.billing.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.order.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem extends BaseEntity {

    //------------------------------------------------
    // INVOICE
    //------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    //------------------------------------------------
    // ORDER ITEM
    //------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    //------------------------------------------------
    // DATA
    //------------------------------------------------

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(precision = 10, scale = 2)
    private BigDecimal rate;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;
}