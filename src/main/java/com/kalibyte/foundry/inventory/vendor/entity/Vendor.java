package com.kalibyte.foundry.inventory.vendor.entity;

import com.kalibyte.foundry.inventory.common.BaseInventoryEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vendors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor extends BaseInventoryEntity {

    @Column(nullable = false)
    private String name;

    private String phone;

    @Column(name = "gst_number")
    private String gstNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
