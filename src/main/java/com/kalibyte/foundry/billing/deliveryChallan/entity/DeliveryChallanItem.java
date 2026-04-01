package com.kalibyte.foundry.billing.deliveryChallan.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.order.entity.OrderItem;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery_challan_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryChallanItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dc_id", nullable = false)
    private DeliveryChallan deliveryChallan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    private Integer quantity;

    private BigDecimal weight;

    private BigDecimal rate;

    private BigDecimal amount;

    // GST per DC item (inherited from order item's GST%)
    @Column(name = "gst_percentage", precision = 5, scale = 2)
    private BigDecimal gstPercentage;

    @Column(name = "gst_amount", precision = 19, scale = 2)
    private BigDecimal gstAmount;

    @Column(name = "total_with_gst", precision = 19, scale = 2)
    private BigDecimal totalWithGst;
}