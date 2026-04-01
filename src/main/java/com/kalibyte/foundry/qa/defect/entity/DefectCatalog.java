package com.kalibyte.foundry.qa.defect.entity;

import com.kalibyte.foundry.qa.common.base.BaseQaEntity;
import com.kalibyte.foundry.qa.common.enums.DefectCategory;
import com.kalibyte.foundry.qa.common.enums.Severity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "qa_defect_catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectCatalog extends BaseQaEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DefectCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Severity severity = Severity.MAJOR;

    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
