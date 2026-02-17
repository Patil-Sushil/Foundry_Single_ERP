package com.kalibyte.foundry.quotation.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.quotation.entity.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "quotation_approvals",
        indexes = {
                @Index(name = "idx_quotation_level", columnList = "quotation_id, approval_level"),
                @Index(name = "idx_approver", columnList = "approver_id")
        })
public class QuotationApproval extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @Column(name = "approval_level", nullable = false)
    private Integer approvalLevel;

    @Column(name = "approver_id")
    private UUID approverId;

    @Column(name = "approver_name", length = 255)
    private String approverName;  // Denormalized for easier querying

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(length = 1000)
    private String comments;

    @Column(name = "action_date")
    private LocalDateTime actionDate;

    @CreationTimestamp
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;

    // Helper method to approve
    public void approve(String comments) {
        this.status = ApprovalStatus.APPROVED;
        this.comments = comments;
        this.actionDate = LocalDateTime.now();
    }

    // Helper method to reject
    public void reject(String comments) {
        this.status = ApprovalStatus.REJECTED;
        this.comments = comments;
        this.actionDate = LocalDateTime.now();
    }

    // Check if pending
    @Transient
    public boolean isPending() {
        return ApprovalStatus.PENDING.equals(this.status);
    }
}