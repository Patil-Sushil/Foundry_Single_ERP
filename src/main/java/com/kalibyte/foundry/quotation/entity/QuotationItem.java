package com.kalibyte.foundry.quotation.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.quotation.entity.enums.PatternStatus;
import jakarta.persistence.*;
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

    // ================= PRICING =================

    @Column(name = "quantity", precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", precision = 19, scale = 2)
    private BigDecimal lineTotal;

    // ================= BUSINESS METHOD =================

    public void calculateLineTotal() {

        if (netWeightKg != null && unitPrice != null && quantity != null) {

            this.lineTotal =
                    netWeightKg
                            .multiply(unitPrice)
                            .multiply(quantity);
        }
    }
}