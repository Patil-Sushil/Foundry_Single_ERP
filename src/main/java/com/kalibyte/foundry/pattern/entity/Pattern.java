package com.kalibyte.foundry.pattern.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.pattern.entity.enums.PatternMaterial;
import com.kalibyte.foundry.pattern.entity.enums.PatternStatus;
import com.kalibyte.foundry.pattern.entity.enums.PatternType;
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

    @Column(name = "pattern_number", nullable = false, unique = true, updatable = false)
    private String patternNumber;

    @Column(nullable = false)
    private String patternName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatternType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatternMaterial material;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatternStatus status;

    @Column(name = "rack_number")
    private String rackNumber;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = PatternStatus.AVAILABLE; // Default
        }
    }
}