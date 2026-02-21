package com.kalibyte.foundry.enquiry.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "enquiry",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "enquiry_no"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enquiry extends BaseEntity {

    @Column(name = "enquiry_no", nullable = false, unique = true)
    private String enquiryNo;

    @Column(name = "enquiry_date", nullable = false)
    private LocalDate enquiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;



    @Column(nullable = false)
    private String status;

    @Column(name = "total_weight_kg", nullable = false)
    private BigDecimal totalWeightKg;

    @OneToMany(
            mappedBy = "enquiry",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default  // Required for @Builder with default value
    private List<EnquiryItem> enquiryItems = new ArrayList<>();
}