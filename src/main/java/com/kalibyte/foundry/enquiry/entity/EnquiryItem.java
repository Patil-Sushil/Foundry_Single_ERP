package com.kalibyte.foundry.enquiry.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.enquiry.entity.ENUM.CastingProcess;
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

    @ManyToOne
    @JoinColumn(name = "metal_category_id", nullable = false)
    private MetalCategory metalCategory;

    @ManyToOne
    @JoinColumn(name = "metal_type_id", nullable = false)
    private MetalType metalType;

    @Column(name = "required_quantity", nullable = false)
    private Integer requiredQuantity;

    @Column(name = "approx_piece_weight_kg", nullable = false)
    private BigDecimal approxPieceWeightKg;

    @Column(name = "total_weight_kg", nullable = false)
    private BigDecimal totalWeightKg;

    @Column(name = "casting_process", nullable = false)
    private String castingProcess;

    @Column(name = "pattern_available", nullable = false)
    private Boolean patternAvailable;

    @Column(name = "machine_required", nullable = false)
    private Boolean machineRequired;
}
