package com.kalibyte.foundry.quotation.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.pattern.entity.Pattern;
import com.kalibyte.foundry.pattern.entity.PatternReceipt;
import com.kalibyte.foundry.quotation.entity.enums.PatternStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "quotation_items")
public class QuotationItem extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    // ================= PART INFO =================

    @Column(name = "part_name", length = 255)
    private String partName;

    @Column(name = "drawing_number", length = 100)
    private String drawingNumber;

    @Column(name = "material_grade", length = 100)
    private String materialGrade;

    // ================= WEIGHT =================

    @Column(name = "net_weight_kg", precision = 10, scale = 3)
    private BigDecimal netWeightKg;

    @Column(name = "gross_weight_kg", precision = 10, scale = 3)
    private BigDecimal grossWeightKg;

    // ================= PATTERN =================

    @Enumerated(EnumType.STRING)
    @Column(name = "pattern_status", length = 20)
    private PatternStatus patternStatus;

    @Column(name = "pattern_provided_by_customer")
    private Boolean patternProvidedByCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pattern_id")
    private Pattern pattern;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pattern_receipt_id")
    private PatternReceipt patternReceipt;

    // ================= PRICING =================

    @Min(1)
    @Column(name = "quantity")
    private int quantity;

    @Column(name = "unit_price", precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", precision = 19, scale = 2)
    private BigDecimal lineTotal;

    // ================= BUSINESS METHOD =================

    public void calculateLineTotal() {
        // quantity is primitive, so we check > 0 instead of != null
        if (netWeightKg != null && unitPrice != null && quantity > 0) {
            this.lineTotal = netWeightKg
                    .multiply(unitPrice)
                    .multiply(BigDecimal.valueOf(quantity));
        }
    }
}