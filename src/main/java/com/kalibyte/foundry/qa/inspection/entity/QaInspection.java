package com.kalibyte.foundry.qa.inspection.entity;

import com.kalibyte.foundry.furnace.furnace_heats.entity.HeatOrderItem;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.production.entity.ProductionEntry;
import com.kalibyte.foundry.production.entity.ProductionItem;
import com.kalibyte.foundry.qa.common.base.BaseQaEntity;
import com.kalibyte.foundry.qa.common.enums.InspectionResult;
import com.kalibyte.foundry.qa.common.enums.InspectionStage;
import com.kalibyte.foundry.qa.common.enums.InspectionStatus;
import com.kalibyte.foundry.qa.common.enums.InspectionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "qa_inspections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QaInspection extends BaseQaEntity {

    @Column(name = "inspection_number", nullable = false, unique = true)
    private String inspectionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_entry_id", nullable = false)
    private ProductionEntry productionEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_item_id", nullable = false)
    private ProductionItem productionItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "heat_order_item_id")
    private HeatOrderItem heatOrderItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_stage", nullable = false)
    private InspectionStage inspectionStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_type", nullable = false)
    @Builder.Default
    private InspectionType inspectionType = InspectionType.VISUAL;

    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @Column(name = "inspector_name", nullable = false)
    private String inspectorName;

    @Column(name = "total_inspected", nullable = false)
    @Builder.Default
    private Integer totalInspected = 0;

    @Column(name = "total_accepted", nullable = false)
    @Builder.Default
    private Integer totalAccepted = 0;

    @Column(name = "total_rejected", nullable = false)
    @Builder.Default
    private Integer totalRejected = 0;

    @Column(name = "total_reworkable", nullable = false)
    @Builder.Default
    private Integer totalReworkable = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InspectionResult result = InspectionResult.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InspectionStatus status = InspectionStatus.DRAFT;

    private String remarks;

    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InspectionFinding> findings = new ArrayList<>();

    public void addFinding(InspectionFinding finding) {
        findings.add(finding);
        finding.setInspection(this);
    }

    public void removeFinding(InspectionFinding finding) {
        findings.remove(finding);
        finding.setInspection(null);
    }
}
