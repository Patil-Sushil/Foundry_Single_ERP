package com.kalibyte.foundry.scrap.entity;

import com.kalibyte.foundry.scrap.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scrap_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scrap_number", nullable = false, unique = true)
    private String scrapNumber;

    @Builder.Default
    @Column(name = "scrap_date", nullable = false)
    private LocalDate scrapDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "scrap_source", nullable = false)
    private ScrapSource scrapSource;

    @Column(name = "source_reference_id")
    private String sourceReferenceId;

    @Column(name = "source_reference_type")
    private String sourceReferenceType;

    @Column(name = "heat_id")
    private Long heatId;

    @Column(name = "inspection_id")
    private Long inspectionId;

    @Column(name = "customer_return_id")
    private Long customerReturnId;

    @Column(name = "qa_rejection_id")
    private Long qaRejectionId;

    @Column(name = "rejection_number")
    private String rejectionNumber;

    @Column(name = "return_number")
    private String returnNumber;

    private String grade;

    @Column(name = "total_weight", nullable = false)
    private BigDecimal totalWeight;

    @Column(name = "total_value")
    private BigDecimal totalValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "confidence_level")
    @Builder.Default
    private ConfidenceLevel confidenceLevel = ConfidenceLevel.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_method")
    private VerificationMethod verificationMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "physical_condition")
    private PhysicalCondition physicalCondition;

    @Column(name = "visual_grade_assessment")
    private String visualGradeAssessment;

    @Column(name = "requires_testing")
    @Builder.Default
    private Boolean requiresTesting = false;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_decision")
    private ApprovalDecision approvalDecision;

    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;

    @Column(name = "final_grade")
    private String finalGrade;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "inward_confirmed_by")
    private String inwardConfirmedBy;

    @Column(name = "inward_confirmed_at")
    private LocalDateTime inwardConfirmedAt;

    @Column(name = "material_inward_id")
    private Long materialInwardId;

    @Column(name = "inventory_item_id")
    private Long inventoryItemId;

    @Column(name = "scrap_sale_id")
    private Long scrapSaleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ScrapStatus status = ScrapStatus.PENDING_VERIFICATION;

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

    @Builder.Default
    @OneToMany(mappedBy = "scrapEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScrapItem> scrapItems = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addScrapItem(ScrapItem item) {
        if (scrapItems == null) {
            scrapItems = new ArrayList<>();
        }
        scrapItems.add(item);
        item.setScrapEntry(this);
    }
}
