package com.kalibyte.foundry.production.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.production.entity.enums.ProductionShift;
import com.kalibyte.foundry.production.entity.enums.ProductionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_entries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductionEntry extends BaseEntity {

    @Column(name = "entry_number", nullable = false, unique = true)
    private String entryNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductionShift shift;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductionStatus status = ProductionStatus.IN_PROGRESS;

    private String operatorName;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Builder.Default
    private Integer totalReadyCores = 0;

    @Builder.Default
    private Integer totalPouredMoulds = 0;

    @Builder.Default
    private Integer totalShotBlastingQuantity = 0;

    @Builder.Default
    private Integer totalFettlingQuantity = 0;

    @Builder.Default
    private Integer totalDispatchedQuantity = 0;

    @Builder.Default
    @Column(name = "total_inspected_quantity", nullable = false)
    private Integer totalInspectedQuantity = 0;

    @Builder.Default
    @Column(name = "total_accepted_quantity", nullable = false)
    private Integer totalAcceptedQuantity = 0;

    @Builder.Default
    @Column(name = "total_rejected_quantity", nullable = false)
    private Integer totalRejectedQuantity = 0;

    @Builder.Default
    @Column(name = "total_rework_quantity", nullable = false)
    private Integer totalReworkQuantity = 0;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "productionEntry",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<ProductionItem> productionItems = new ArrayList<>();

    public void recalculateTotals() {
        this.totalReadyCores = productionItems.stream().mapToInt(ProductionItem::getReadyCores).sum();
        this.totalPouredMoulds = productionItems.stream().mapToInt(ProductionItem::getPouredMoulds).sum();
        this.totalShotBlastingQuantity = productionItems.stream().mapToInt(ProductionItem::getShotBlastingQuantity).sum();
        this.totalFettlingQuantity = productionItems.stream().mapToInt(ProductionItem::getFettlingQuantity).sum();
        this.totalDispatchedQuantity = productionItems.stream().mapToInt(ProductionItem::getDispatchedQuantity).sum();
        
        this.totalInspectedQuantity = productionItems.stream().mapToInt(ProductionItem::getInspectedQuantity).sum();
        this.totalAcceptedQuantity = productionItems.stream().mapToInt(ProductionItem::getAcceptedQuantity).sum();
        this.totalRejectedQuantity = productionItems.stream().mapToInt(ProductionItem::getRejectedQuantity).sum();
        this.totalReworkQuantity = productionItems.stream().mapToInt(ProductionItem::getReworkQuantity).sum();
    }
}
