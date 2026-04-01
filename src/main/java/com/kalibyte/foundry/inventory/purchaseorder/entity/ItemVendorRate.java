package com.kalibyte.foundry.inventory.purchaseorder.entity;

import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "item_vendor_rates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"item_id", "vendor_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemVendorRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "last_rate", nullable = false)
    private BigDecimal lastRate;

    @Column(name = "last_purchased_on", nullable = false)
    private LocalDate lastPurchasedOn;
}
