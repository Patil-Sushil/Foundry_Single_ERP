package com.kalibyte.foundry.inventory.item.entity;

import com.kalibyte.foundry.inventory.common.BaseInventoryEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stock_adjustments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAdjustment extends BaseInventoryEntity {

    @Column(name = "adjustment_number", nullable = false, unique = true)
    private String adjustmentNumber;

    @Column(name = "adjustment_date", nullable = false)
    private LocalDate adjustmentDate;

    @Column(nullable = false)
    private String reason;

    @Column(name = "adjusted_by_user_id")
    private UUID adjustedByUserId;

    @OneToMany(mappedBy = "stockAdjustment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AdjustmentItem> items = new ArrayList<>();

    public void addItem(AdjustmentItem item) {
        items.add(item);
        item.setStockAdjustment(this);
    }
}
