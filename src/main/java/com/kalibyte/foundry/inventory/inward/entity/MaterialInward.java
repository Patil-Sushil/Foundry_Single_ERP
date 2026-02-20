package com.kalibyte.foundry.inventory.inward.entity;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.inventory.common.BaseInventoryEntity;
import com.kalibyte.foundry.inventory.inward.entity.enums.InwardStatus;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "material_inwards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialInward extends BaseInventoryEntity {

    @Column(name = "inward_number", nullable = false, unique = true)
    private String inwardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_phone")
    private String driverPhone;

    @Column(name = "vendor_challan_number")
    private String vendorChallanNumber;

    @Builder.Default
    @Column(name = "inward_date", nullable = false)
    private LocalDate inwardDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private InwardStatus status = InwardStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "confirmed_by_user_id")
    private UUID confirmedByUserId;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Builder.Default
    @OneToMany(mappedBy = "materialInward", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceivedItem> receivedItems = new ArrayList<>();

    // --- DOMAIN METHODS ---

    public void addReceivedItem(ReceivedItem item) {
        if (this.status != InwardStatus.DRAFT) {
            throw new BusinessException("Cannot add items to a confirmed inward.");
        }
        item.setMaterialInward(this);
        this.receivedItems.add(item);
    }

    public void confirm(UUID userId) {
        if (this.status != InwardStatus.DRAFT) {
            throw new BusinessException("Already confirmed.");
        }
        if (this.receivedItems.isEmpty()) {
            throw new BusinessException("Cannot confirm inward with no items.");
        }
        this.status = InwardStatus.CONFIRMED;
        this.confirmedByUserId = userId;
        this.confirmedAt = LocalDateTime.now();
    }

    public BigDecimal getTotalAmount() {
        return receivedItems.stream()
                .map(ReceivedItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isDraft() {
        return this.status == InwardStatus.DRAFT;
    }
}
