package com.kalibyte.foundry.enquiry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "metal_types",
        uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "name"})
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetalType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "category_id")
    private MetalCategory category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean active = true;



}
