package com.kalibyte.foundry.qa.inspection.entity;

import com.kalibyte.foundry.qa.common.base.BaseQaEntity;
import com.kalibyte.foundry.qa.common.enums.FindingDisposition;
import com.kalibyte.foundry.qa.defect.entity.DefectCatalog;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "qa_inspection_findings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionFinding extends BaseQaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id", nullable = false)
    private QaInspection inspection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defect_id", nullable = false)
    private DefectCatalog defect;

    @Column(name = "quantity_affected", nullable = false)
    @Builder.Default
    private Integer quantityAffected = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FindingDisposition disposition = FindingDisposition.REJECT;

    @Column(name = "rework_instruction")
    private String reworkInstruction;

    @Column(name = "photo_urls")
    private List<String> photoUrls;

    private String remarks;
}
