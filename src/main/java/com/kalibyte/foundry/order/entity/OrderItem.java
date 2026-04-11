package com.kalibyte.foundry.order.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.enquiry.entity.enums.MetalCategory;
import com.kalibyte.foundry.enquiry.entity.enums.MetalType;
import com.kalibyte.foundry.pattern.entity.Pattern;
import com.kalibyte.foundry.pattern.entity.PatternReceipt;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // ================= PART INFO =================

    @Column(name = "part_name", nullable = false, length = 255)
    private String partName;

    @Column(name = "drawing_number", length = 100)
    private String drawingNumber;

    @Column(name = "material_grade", length = 100)
    private String materialGrade;

    // ================= METAL & CASTING =================

    @Enumerated(EnumType.STRING)
    @Column(name = "metal_type", length = 50)
    private MetalType metalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "metal_category", length = 50)
    private MetalCategory metalCategory;

    @Column(name = "casting_process", length = 50)
    private String castingProcess;

    @Column(name = "is_machining_required")
    private Boolean isMachiningRequired = false;

    // ================= WEIGHT =================

    @Column(name = "net_weight_kg", precision = 10, scale = 3)
    private BigDecimal netWeightKg;

    @Column(name = "gross_weight_kg", precision = 10, scale = 3)
    private BigDecimal grossWeightKg;

    // ================= PATTERN =================

    @Column(name = "pattern_provided_by_customer")
    private Boolean patternProvidedByCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pattern_id")
    private Pattern pattern;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pattern_receipt_id")
    private PatternReceipt patternReceipt;

    // ================= PRICING =================
//    @Column(name = "discount", precision = 5, scale = 2)
//    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", precision = 19, scale = 2)
    private BigDecimal lineTotal;

    // ================= GST PER ITEM =================

    @Column(name = "gst_percentage", precision = 5, scale = 2)
    private BigDecimal gstPercentage;

    @Column(name = "gst_amount", precision = 19, scale = 2)
    private BigDecimal gstAmount;

    @Column(name = "total_with_gst", precision = 19, scale = 2)
    private BigDecimal totalWithGst;

    // ================= PRODUCTION TRACKING =================

    @Column(name = "produced_quantity")
    @Builder.Default
    private Integer producedQuantity = 0;

    @Column(name = "dispatched_quantity")
    @Builder.Default
    private Integer dispatchedQuantity = 0;

    // ================= BUSINESS METHODS =================

    public void calculateLineTotal() {
        if (netWeightKg != null && unitPrice != null && quantity != null && quantity > 0) {
            this.lineTotal = netWeightKg
                    .multiply(unitPrice)
                    .multiply(BigDecimal.valueOf(quantity));
        }
    }

    public void calculateGst(BigDecimal gstPct) {
        this.gstPercentage = gstPct;
        if (lineTotal != null && gstPct != null) {
            this.gstAmount = lineTotal.multiply(gstPct)
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            this.totalWithGst = lineTotal.add(this.gstAmount);
        }
    }

    public Integer getPendingQuantity() {
        int produced = producedQuantity != null ? producedQuantity : 0;
        return quantity - produced;
    }

    public boolean isFullyProduced() {
        return producedQuantity != null && producedQuantity.equals(quantity);
    }

    public boolean isFullyDispatched() {
        return dispatchedQuantity != null && dispatchedQuantity.equals(quantity);
    }
}