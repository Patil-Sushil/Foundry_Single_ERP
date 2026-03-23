package com.kalibyte.foundry.order.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.pattern.entity.Pattern;
import com.kalibyte.foundry.pattern.entity.PatternReceipt;
import com.kalibyte.foundry.quotation.entity.enums.PatternStatus;
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

    //------------------------------------------------
    // ORDER
    //------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    //------------------------------------------------
    // PART INFO
    //------------------------------------------------
    private String partName;
    private String drawingNumber;
    private String materialGrade;

    //------------------------------------------------
    // WEIGHT
    //------------------------------------------------
    private BigDecimal netWeightKg;
    private BigDecimal grossWeightKg;

    //------------------------------------------------
    // PATTERN LOGIC (SAME AS QUOTATION)
    //------------------------------------------------
    @Enumerated(EnumType.STRING)
    private PatternStatus patternStatus;

    private Boolean patternProvidedByCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pattern_id")
    private Pattern pattern;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pattern_receipt_id")
    private PatternReceipt patternReceipt;

    //------------------------------------------------
    // PRICING
    //------------------------------------------------
    private int quantity;

    private BigDecimal unitPrice;

    private BigDecimal lineTotal;

    //------------------------------------------------
    // BUSINESS LOGIC
    //------------------------------------------------
    public void calculateLineTotal() {
        if (netWeightKg != null && unitPrice != null && quantity > 0) {
            this.lineTotal = netWeightKg
                    .multiply(unitPrice)
                    .multiply(BigDecimal.valueOf(quantity));
        }
    }
}