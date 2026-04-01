package com.kalibyte.foundry.inventory.issue.entity;

import com.kalibyte.foundry.inventory.common.BaseInventoryEntity;
import com.kalibyte.foundry.inventory.department.entity.Department;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "material_issues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialIssue extends BaseInventoryEntity {

    @Column(name = "issue_number", nullable = false, unique = true)
    private String issueNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "issued_by_user_id")
    private UUID issuedByUserId;

    @Builder.Default
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate = LocalDate.now();

    @Column(length = 500)
    private String purpose;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @OneToMany(mappedBy = "materialIssue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IssuedItem> issuedItems = new ArrayList<>();

    // --- DOMAIN METHODS ---

    public void addIssuedItem(IssuedItem item) {
        item.setMaterialIssue(this);
        this.issuedItems.add(item);
    }

    public BigDecimal getTotalValue() {
        return issuedItems.stream()
                .map(IssuedItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
