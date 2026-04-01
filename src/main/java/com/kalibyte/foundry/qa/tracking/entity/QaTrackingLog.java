package com.kalibyte.foundry.qa.tracking.entity;

import com.kalibyte.foundry.qa.common.enums.TrackingAction;
import com.kalibyte.foundry.qa.common.enums.TrackingReferenceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "qa_tracking_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QaTrackingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false)
    private TrackingReferenceType referenceType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "from_status")
    private String fromStatus;

    @Column(name = "to_status", nullable = false)
    private String toStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrackingAction action;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
