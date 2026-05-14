package com.kalibyte.foundry.common.castingprocess.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "casting_processes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CastingProcessMaster extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
