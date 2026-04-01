package com.kalibyte.foundry.inventory.issue.entity;

import com.kalibyte.foundry.inventory.item.entity.Item;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "issued_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssuedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_issue_id", nullable = false)
    private MaterialIssue materialIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "issued_quantity", nullable = false)
    private BigDecimal issuedQuantity;

    @Column(name = "unit_rate", nullable = false)
    private BigDecimal unitRate;

    private String notes;

    // --- DOMAIN METHODS ---

    public BigDecimal getAmount() {
        return issuedQuantity.multiply(unitRate).setScale(2, RoundingMode.HALF_UP);
    }
}
