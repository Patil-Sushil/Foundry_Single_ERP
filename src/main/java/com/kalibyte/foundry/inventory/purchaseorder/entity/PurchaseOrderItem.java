package com.kalibyte.foundry.inventory.purchaseorder.entity;

import com.kalibyte.foundry.inventory.item.entity.Item;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "purchase_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "ordered_quantity", nullable = false)
    private BigDecimal orderedQuantity;

    @Builder.Default
    @Column(name = "received_quantity")
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    @Column(name = "unit_rate", nullable = false)
    private BigDecimal unitRate;

    @Column(name = "gst_rate")
    private BigDecimal gstRate;

    @Column(name = "hsn_code")
    private String hsnCode;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    private String notes;

    // --- DOMAIN METHODS ---

    public BigDecimal getTaxableValue() {
        return orderedQuantity.multiply(unitRate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalValue() {
        BigDecimal tax = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        return getTaxableValue().add(tax).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPendingQuantity() {
        return orderedQuantity.subtract(receivedQuantity);
    }

    public boolean isFullyReceived() {
        return receivedQuantity.compareTo(orderedQuantity) >= 0;
    }

    public void addReceivedQuantity(BigDecimal qty) {
        this.receivedQuantity = this.receivedQuantity.add(qty);
    }
}
