package com.kalibyte.foundry.inventory.item.entity;

import com.kalibyte.foundry.inventory.common.BaseInventoryEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "adjustment_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjustmentItem extends BaseInventoryEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_adjustment_id", nullable = false)
    private StockAdjustment stockAdjustment;

    @Column(name = "adjusted_quantity", nullable = false)
    private BigDecimal adjustedQuantity;

    @Column(name = "unit_rate")
    private BigDecimal unitRate;
}
