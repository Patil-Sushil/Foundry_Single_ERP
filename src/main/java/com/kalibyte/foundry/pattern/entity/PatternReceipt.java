package com.kalibyte.foundry.pattern.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.enquiry.entity.EnquiryItem;
import com.kalibyte.foundry.pattern.entity.enums.PatternMaterial;
import com.kalibyte.foundry.pattern.entity.enums.PatternType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Builder
@Getter
@Setter
@Table(name = "pattern_receipt")
@NoArgsConstructor
@AllArgsConstructor
public class PatternReceipt extends BaseEntity {

    @Column(name = "inward_date")
    private LocalDate inwardDate;

    @Column(name = "outward_date")
    private LocalDate outwardDate;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatternType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatternMaterial material;

    @OneToOne(mappedBy = "patternReceipt")
    private EnquiryItem enquiryItem;
}
