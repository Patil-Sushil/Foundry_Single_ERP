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
    @Column(name = "total_taxable_amount")
    private BigDecimal totalTaxableAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "cgst")
    private BigDecimal cgst = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "sgst")
    private BigDecimal sgst = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "igst")
    private BigDecimal igst = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_tax_amount")
    private BigDecimal totalTaxAmount = BigDecimal.ZERO;

    @Column(name = "gst_type")
    @Enumerated(EnumType.STRING)
    private com.kalibyte.foundry.order.entity.enums.GstType gstType;

    @Builder.Default
    @Column(name = "grand_total")
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Builder.Default
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> orderItems = new ArrayList<>();

    // --- DOMAIN METHODS ---

    public void calculateTotals(String companyState) {
        this.totalTaxableAmount = orderItems.stream()
                .map(PurchaseOrderItem::getTaxableValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // We calculate total tax amount first
        BigDecimal totalTax = orderItems.stream()
                .map(item -> item.getTaxAmount() != null ? item.getTaxAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalTaxAmount = totalTax;

        // Determine GST breakdown based on state
        boolean isSameState = companyState != null && 
                             companyState.equalsIgnoreCase(vendor != null && vendor.getState() != null ? vendor.getState().trim() : "");

        if (isSameState) {
            this.gstType = com.kalibyte.foundry.order.entity.enums.GstType.CGST_SGST;
            this.cgst = totalTax.divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP);
            this.sgst = totalTax.subtract(this.cgst);
            this.igst = BigDecimal.ZERO;
        } else {
            this.gstType = com.kalibyte.foundry.order.entity.enums.GstType.IGST;
            this.igst = totalTax;
            this.cgst = BigDecimal.ZERO;
            this.sgst = BigDecimal.ZERO;
        }

        this.grandTotal = totalTaxableAmount.add(totalTax);
    }

    public void calculateTotals() {
        calculateTotals("Maharashtra");
    }

    public void addOrderItem(PurchaseOrderItem item) {
        item.setPurchaseOrder(this);
        this.orderItems.add(item);
    }

    public BigDecimal getTotalOrderValue() {
        return grandTotal;
    }

    public void cancel() {
        if (this.status != POStatus.OPEN) {
            throw new BusinessException("Only OPEN orders can be cancelled.");
        }
        this.status = POStatus.CANCELLED;
    }

    public void updateStatusAfterInward() {
        boolean allReceived = orderItems.stream().allMatch(PurchaseOrderItem::isFullyReceived);
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
