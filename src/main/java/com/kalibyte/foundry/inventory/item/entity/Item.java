package com.kalibyte.foundry.inventory.item.entity;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.inventory.common.BaseInventoryEntity;
import com.kalibyte.foundry.inventory.department.entity.Department;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemSubCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemUnit;
import com.kalibyte.foundry.inventory.item.entity.enums.StockStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item extends BaseInventoryEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_category")
    private ItemSubCategory subCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemUnit unit;

    @Builder.Default
    @Column(name = "current_stock")
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "reorder_level")
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "min_stock_level")
    private BigDecimal minStockLevel = BigDecimal.ZERO;

    private String location;

    @Builder.Default
    @Column(name = "last_purchase_rate")
    private BigDecimal lastPurchaseRate = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "avg_rate")
    private BigDecimal avgRate = BigDecimal.ZERO;

    @Column(name = "hsn_code")
    private String hsnCode;

    @Builder.Default
    @Column(name = "gst_rate")
    private BigDecimal gstRate = new BigDecimal("18.00");

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    // --- DOMAIN METHODS ---

    public void receiveStock(BigDecimal incomingQuantity, BigDecimal purchaseRate) {
        if (incomingQuantity.compareTo(BigDecimal.ZERO) <= 0 || purchaseRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Incoming quantity and purchase rate must be positive.");
        }

        BigDecimal existingValue = currentStock.multiply(avgRate);
        BigDecimal incomingValue = incomingQuantity.multiply(purchaseRate);
        BigDecimal newTotalQty = currentStock.add(incomingQuantity);
        
        // Avoid division by zero (should not happen if incoming > 0)
        BigDecimal newAvgRate = existingValue.add(incomingValue)
                .divide(newTotalQty, 2, RoundingMode.HALF_UP);

        this.currentStock = newTotalQty;
        this.avgRate = newAvgRate;
        this.lastPurchaseRate = purchaseRate;
    }

    public void issueStock(BigDecimal issueQuantity) {
        if (issueQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Issue quantity must be positive.");
        }
        if (issueQuantity.compareTo(currentStock) > 0) {
            throw new BusinessException(String.format("Insufficient stock for %s. Available: %s, Requested: %s", 
                    this.name, this.currentStock, issueQuantity));
        }

        this.currentStock = this.currentStock.subtract(issueQuantity);
        // avgRate does NOT change on issue
    }

    public BigDecimal getStockValue() {
        return currentStock.multiply(avgRate).setScale(2, RoundingMode.HALF_UP);
    }

    public StockStatus getStockStatus() {
        if (currentStock.compareTo(minStockLevel) <= 0) {
            return StockStatus.CRITICAL;
        }
        if (currentStock.compareTo(reorderLevel) <= 0) {
            return StockStatus.LOW;
        }
        return StockStatus.OK;
    }
}
