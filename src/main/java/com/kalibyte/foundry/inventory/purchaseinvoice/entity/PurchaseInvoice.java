package com.kalibyte.foundry.inventory.purchaseinvoice.entity;

import com.kalibyte.foundry.inventory.inward.entity.MaterialInward;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchase_invoices",
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uk_vendor_invoice",
               columnNames = {"vendor_id", "vendor_invoice_number"}
           )
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseInvoice {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_invoice_number", nullable = false, length = 50)
    private String vendorInvoiceNumber;

    @Column(name = "vendor_invoice_date", nullable = false)
    private LocalDate vendorInvoiceDate;

    @Column(name = "invoice_amount", precision = 12, scale = 2)
    private BigDecimal invoiceAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_inward_id")
    private MaterialInward materialInward;

    @Column(length = 500)
    private String remarks;

    @Column(name = "source", length = 10, nullable = false)
    @Builder.Default
    private String source = "AUTO";

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verified_by_user_id")
    private Long verifiedByUserId;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void verify(Long userId) {
        this.isVerified = true;
        this.verifiedByUserId = userId;
        this.verifiedAt = LocalDateTime.now();
    }

    public BigDecimal getAmountMismatch() {
        if (materialInward == null || invoiceAmount == null || materialInward.getTotalAmount() == null) {
            return null;
        }
        return invoiceAmount.subtract(materialInward.getTotalAmount());
    }

    public boolean hasAmountMismatch() {
        BigDecimal mismatch = getAmountMismatch();
        return mismatch != null && mismatch.abs().compareTo(BigDecimal.ONE) > 0;
    }
}
