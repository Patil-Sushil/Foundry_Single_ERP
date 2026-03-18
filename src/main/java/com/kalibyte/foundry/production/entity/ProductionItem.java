package com.kalibyte.foundry.production.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.order.entity.OrderItem;
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

    @Column(nullable = false)
    private String itemName;

    private String patternNumber;

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

    private String itemRemark;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
}
