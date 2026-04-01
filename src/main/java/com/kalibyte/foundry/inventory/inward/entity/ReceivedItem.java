package com.kalibyte.foundry.inventory.inward.entity;

import com.kalibyte.foundry.inventory.inward.entity.enums.ReceiptStatus;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "received_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceivedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_inward_id", nullable = false)
    private MaterialInward materialInward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private PurchaseOrderItem orderItem;

    @Column(name = "po_quantity")
    private BigDecimal poQuantity;

    @Column(name = "received_quantity", nullable = false)
    private BigDecimal receivedQuantity;

    @Column(name = "unit_rate", nullable = false)
    private BigDecimal unitRate;

    @Column(name = "gst_rate")
    private BigDecimal gstRate;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @Column(name = "amount")
    private BigDecimal amount;

    private String notes;

    // --- DOMAIN METHODS ---

    public BigDecimal getTaxableAmount() {
        return receivedQuantity.multiply(unitRate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAmount() {
        return amount != null ? amount : getTaxableAmount().add(taxAmount != null ? taxAmount : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getQuantityDifference() {
        if (poQuantity == null) {
            return BigDecimal.ZERO;
        }
        return receivedQuantity.subtract(poQuantity);
    }

    public ReceiptStatus getReceiptStatus() {
        BigDecimal diff = getQuantityDifference();
        int compare = diff.compareTo(BigDecimal.ZERO);
        if (compare < 0) {
            return ReceiptStatus.SHORT;
        } else if (compare > 0) {
            return ReceiptStatus.EXCESS;
        } else {
            return ReceiptStatus.OK;
        }
    }
}
