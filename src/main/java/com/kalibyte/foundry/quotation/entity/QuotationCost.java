package com.kalibyte.foundry.quotation.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "quotation_costs")
public class QuotationCost extends BaseEntity {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_item_id", nullable = false, unique = true)
    private QuotationItem quotationItem;

    @Column(name = "metal_cost", precision = 19, scale = 2)
    private BigDecimal metalCost = BigDecimal.ZERO;  //  Default to ZERO

    @Column(name = "moulding_cost", precision = 19, scale = 2)
    private BigDecimal mouldingCost = BigDecimal.ZERO;

    @Column(name = "melting_cost", precision = 19, scale = 2)
    private BigDecimal meltingCost = BigDecimal.ZERO;

    @Column(name = "machining_cost", precision = 19, scale = 2)
    private BigDecimal machiningCost = BigDecimal.ZERO;

    @Column(name = "overhead_cost", precision = 19, scale = 2)
    private BigDecimal overheadCost = BigDecimal.ZERO;

    @Column(name = "total_cost", precision = 19, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(name = "margin_percent", precision = 5, scale = 2)
    private BigDecimal marginPercent = BigDecimal.ZERO;

    //  Calculate total cost
    public void calculateTotalCost() {
        this.totalCost = safe(metalCost)
                .add(safe(mouldingCost))
                .add(safe(meltingCost))
                .add(safe(machiningCost))
                .add(safe(overheadCost));
    }

    //  Calculate selling price with margin
    public BigDecimal calculateSellingPrice() {
        calculateTotalCost();  // Ensure total is up to date

        if (marginPercent == null || marginPercent.compareTo(BigDecimal.ZERO) == 0) {
            return totalCost;
        }

        BigDecimal marginMultiplier = BigDecimal.ONE.add(
                marginPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );
        return totalCost.multiply(marginMultiplier).setScale(2, RoundingMode.HALF_UP);
    }

    //  Helper: null-safe BigDecimal
    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}