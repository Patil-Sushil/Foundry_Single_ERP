package com.kalibyte.foundry.qa.rejection.entity;

import com.kalibyte.foundry.furnace.furnace_heats.entity.HeatOrderItem;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.production.entity.ProductionEntry;
import com.kalibyte.foundry.production.entity.ProductionItem;
import com.kalibyte.foundry.qa.common.base.BaseQaEntity;
import com.kalibyte.foundry.qa.common.enums.RejectionDisposition;
import com.kalibyte.foundry.qa.common.enums.RejectionStatus;
import com.kalibyte.foundry.qa.defect.entity.DefectCatalog;
import com.kalibyte.foundry.qa.inspection.entity.QaInspection;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "qa_rejections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QaRejection extends BaseQaEntity {

    @Column(name = "rejection_number", nullable = false, unique = true)
    private String rejectionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id", nullable = false)
    private QaInspection inspection;

    @Column(name = "production_entry_id", nullable = false)
    private UUID productionEntryId;

    @Column(name = "production_item_id", nullable = false)
    private UUID productionItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "heat_order_item_id")
    private Long heatOrderItemId;

    @Column(name = "rejected_quantity", nullable = false)
    private Integer rejectedQuantity;

    @Column(name = "rejected_weight")
    private BigDecimal rejectedWeight;

    @Column(name = "unit_weight")
    private BigDecimal unitWeight;

    @Column(name = "material_grade")
    private String materialGrade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_defect_id")
    private DefectCatalog primaryDefect;

    @Column(name = "defect_summary")
    private String defectSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RejectionDisposition disposition = RejectionDisposition.PENDING_REVIEW;

    @Column(name = "disposition_date")
    private LocalDate dispositionDate;

    @Column(name = "disposition_by")
    private String dispositionBy;

    @Column(name = "disposition_remarks")
    private String dispositionRemarks;

    @Column(name = "scrap_entry_id")
    private Long scrapEntryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RejectionStatus status = RejectionStatus.OPEN;
}
