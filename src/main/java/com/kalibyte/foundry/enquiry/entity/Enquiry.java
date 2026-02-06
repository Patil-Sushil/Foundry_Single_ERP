package com.kalibyte.foundry.enquiry.entity;

import aj.org.objectweb.asm.ConstantDynamic;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.enquiry.entity.ENUM.CastingProcess;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(
        name = "enquiry",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","enquiry_no"})
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enquiry {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "enquiry_no", nullable = false, unique = true)
    private String enquiryNo;

    @Column(name = "enquiry_date",nullable = false)
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
    private List<EnquiryItem> enquiryItems = new ArrayList<>();
}