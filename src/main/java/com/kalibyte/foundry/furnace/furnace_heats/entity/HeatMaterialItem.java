package com.kalibyte.foundry.furnace.furnace_heats.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "heat_material_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeatMaterialItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "heat_id", nullable = false)
    private FurnaceHeats heat;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false)
    @Builder.Default
    private HeatMaterialType materialType = HeatMaterialType.RAW_MATERIAL;

    @Column(name = "quantity_used", nullable = false)
    private Double quantityUsed;

    @Column(name = "unit_rate")
    private Double unitRate;

    @Column(name = "total_cost")
    private Double totalCost;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
