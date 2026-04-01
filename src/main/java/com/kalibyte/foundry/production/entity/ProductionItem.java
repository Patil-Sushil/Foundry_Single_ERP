package com.kalibyte.foundry.production.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.pattern.entity.Pattern;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "production_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_entry_id", nullable = false)
    private ProductionEntry productionEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "heat_order_item_id")
    private com.kalibyte.foundry.furnace.furnace_heats.entity.HeatOrderItem heatOrderItem;

    @Column(nullable = false)
    private String itemName;

    @ManyToOne
    @JoinColumn(name = "pattern_id")
    private Pattern pattern;


    @Column(nullable = false)
    private Integer orderedQuantity;

    @Builder.Default
    private Integer readyCores = 0;

    @Builder.Default
    private Integer pouredMoulds = 0;

    @Builder.Default
    private Integer shotBlastingQuantity = 0;

    @Builder.Default
    private Integer fettlingQuantity = 0;

    @Builder.Default
    private Integer dispatchedQuantity = 0;

    @Builder.Default
    @Column(name = "inspected_quantity", nullable = false)
    private Integer inspectedQuantity = 0;

    @Builder.Default
    @Column(name = "accepted_quantity", nullable = false)
    private Integer acceptedQuantity = 0;

    @Builder.Default
    @Column(name = "rejected_quantity", nullable = false)
    private Integer rejectedQuantity = 0;

    @Builder.Default
    @Column(name = "rework_quantity", nullable = false)
    private Integer reworkQuantity = 0;

    private String itemRemark;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
}
