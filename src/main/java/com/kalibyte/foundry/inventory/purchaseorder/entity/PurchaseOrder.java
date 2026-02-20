package com.kalibyte.foundry.inventory.purchaseorder.entity;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.inventory.common.BaseInventoryEntity;
import com.kalibyte.foundry.inventory.purchaseorder.entity.enums.POStatus;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder extends BaseInventoryEntity {

    @Column(name = "po_number", nullable = false, unique = true)
    private String poNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private POStatus status = POStatus.OPEN;

    @Builder.Default
    @Column(name = "po_date", nullable = false)
    private LocalDate poDate = LocalDate.now();

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Builder.Default
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    // --- DOMAIN METHODS ---

    public void addOrderItem(OrderItem item) {
        item.setPurchaseOrder(this);
        this.orderItems.add(item);
    }

    public BigDecimal getTotalOrderValue() {
        return orderItems.stream()
                .map(OrderItem::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void cancel() {
        if (this.status != POStatus.OPEN) {
            throw new BusinessException("Only OPEN orders can be cancelled.");
        }
        this.status = POStatus.CANCELLED;
    }

    public void updateStatusAfterInward() {
        boolean allReceived = orderItems.stream().allMatch(OrderItem::isFullyReceived);
        if (allReceived) {
            this.status = POStatus.RECEIVED;
        } else {
            this.status = POStatus.PARTIALLY_RECEIVED;
        }
    }

    public boolean isOpen() {
        return this.status == POStatus.OPEN || this.status == POStatus.PARTIALLY_RECEIVED;
    }
}
