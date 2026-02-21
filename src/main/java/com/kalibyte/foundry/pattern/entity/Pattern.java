package com.kalibyte.foundry.pattern.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.pattern.entity.ENUMS.PatternMaterial;
import com.kalibyte.foundry.pattern.entity.ENUMS.PatternType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "patterns")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pattern extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatternType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatternMaterial material;
}
