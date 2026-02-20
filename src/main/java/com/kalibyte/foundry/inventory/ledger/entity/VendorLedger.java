package com.kalibyte.foundry.inventory.ledger.entity;

import com.kalibyte.foundry.inventory.inward.entity.MaterialInward;
import com.kalibyte.foundry.inventory.ledger.entity.enums.LedgerEntryType;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_ledger")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_inward_id")
    private MaterialInward materialInward;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private LedgerEntryType entryType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 500)
    private String description;

    @Builder.Default
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate = LocalDate.now();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;
}
