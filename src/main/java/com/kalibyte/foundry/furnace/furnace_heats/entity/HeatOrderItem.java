package com.kalibyte.foundry.furnace.furnace_heats.entity;

import com.kalibyte.foundry.order.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "heat_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeatOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "heat_id", nullable = false)
    private FurnaceHeats heat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Column(name = "quantity_produced", nullable = false)
    private Integer quantityProduced;

    @Column(name = "weight_produced", nullable = false)
    private BigDecimal weightProduced;

    @Column(name = "piece_weight")
    private BigDecimal pieceWeight;

    @Column(name = "stock_item_name")
    private String stockItemName;

    @Column(name = "stock_item_code")
    private String stockItemCode;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
