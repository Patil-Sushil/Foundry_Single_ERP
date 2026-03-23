package com.kalibyte.foundry.enquiry.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.enquiry.entity.enums.MetalCategory;
import com.kalibyte.foundry.enquiry.entity.enums.MetalType;
import com.kalibyte.foundry.enquiry.entity.enums.PatternProvidedBy;
import com.kalibyte.foundry.pattern.entity.Pattern;
import com.kalibyte.foundry.pattern.entity.PatternReceipt;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "enquiry_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_id", nullable = false)
    private Enquiry enquiry;

    @Column(name = "part_name", nullable = false)
    private String partName;

    @Enumerated(EnumType.STRING)
    @Column(name = "metal_category", nullable = false)
    private MetalCategory metalCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "metal_type", nullable = false)
    private MetalType metalType;

    @Column(name = "required_quantity", nullable = false)
    private Integer requiredQuantity;

    @Column(name = "approx_piece_weight_kg", nullable = false)
    private BigDecimal approxPieceWeightKg;

    @Column(name = "total_weight_kg", nullable = false)
    private BigDecimal totalWeightKg;

    @Column(name = "casting_process", nullable = false)
    private String castingProcess;

    // ===== Pattern Logic =====

    @Enumerated(EnumType.STRING)
    @Column(name = "pattern_provided_by", nullable = false)
    private PatternProvidedBy patternProvidedBy;


    @Column(name = "machine_required", nullable = false)
    private Boolean machineRequired;
}