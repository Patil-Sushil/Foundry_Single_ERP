package com.kalibyte.foundry.qa.customerreturn.entity;

import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.qa.common.base.BaseQaEntity;
import com.kalibyte.foundry.qa.common.enums.*;
import com.kalibyte.foundry.qa.inspection.entity.QaInspection;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "qa_customer_returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerReturn extends BaseQaEntity {

    @Column(name = "return_number", nullable = false, unique = true)
    private String returnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "production_entry_id")
    private UUID productionEntryId;

    @Column(name = "heat_order_item_id")
    private Long heatOrderItemId;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "returned_quantity", nullable = false)
    private Integer returnedQuantity;

    @Column(name = "returned_weight")
    private BigDecimal returnedWeight;

    @Column(name = "material_grade")
    private String materialGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "complaint_category", nullable = false)
    private ComplaintCategory complaintCategory;

    @Column(name = "complaint_description", nullable = false)
    private String complaintDescription;

    @Column(name = "customer_reference_no")
    private String customerReferenceNo;

    @Column(name = "qa_assessment_date")
    private LocalDate qaAssessmentDate;

    @Column(name = "qa_inspector_name")
    private String qaInspectorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "qa_finding")
    private QaFinding qaFinding;

    @Column(name = "qa_remarks")
    private String qaRemarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "root_cause_category")
    private RootCauseCategory rootCauseCategory;

    @Column(name = "root_cause_description")
    private String rootCauseDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReturnDisposition disposition = ReturnDisposition.PENDING_ASSESSMENT;

    @Column(name = "disposition_date")
    private LocalDate dispositionDate;

    @Column(name = "disposition_by")
    private String dispositionBy;

    @Column(name = "credit_amount")
    @Builder.Default
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(name = "replacement_order_id")
    private UUID replacementOrderId;

    @Column(name = "credit_note_id")
    private UUID creditNoteId;

    @Column(name = "scrap_entry_id")
    private Long scrapEntryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id")
    private QaInspection inspection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReturnStatus status = ReturnStatus.RECEIVED;
}
