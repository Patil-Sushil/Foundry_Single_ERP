package com.kalibyte.foundry.scrap.entity;

import com.kalibyte.foundry.inventory.item.entity.Item;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scrap_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrap_entry_id", nullable = false)
    private ScrapEntry scrapEntry;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_code")
    private String itemCode;

    private String grade;

    @Column(name = "scrap_type")
    private String scrapType;

    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal weight;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    @Column(name = "total_cost")
    private BigDecimal totalCost;

    @Column(name = "defect_type")
    private String defectType;

    @Column(nullable = false)
    private String recyclability;

    private String destination;

    @Column(name = "material_inward_id")
    private Long materialInwardId;

    @Column(name = "scrap_sale_id")
    private Long scrapSaleId;

    @Column(name = "in_inventory")
    @Builder.Default
    private Boolean inInventory = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id")
    private Item inventoryItem;

    @Column(name = "inspection_defect_id")
    private Long inspectionDefectId;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
