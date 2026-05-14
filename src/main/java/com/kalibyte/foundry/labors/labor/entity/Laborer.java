package com.kalibyte.foundry.labors.labor.entity;

import com.kalibyte.foundry.labors.labor.entity.Enum.WageType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "laborers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Laborer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "ph_number")
    private String phNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "wage_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private WageType wageType; // 'HOURLY' or 'PIECE_RATE'

    @Column(name = "daily_wage",precision = 19, scale = 2)
    private BigDecimal dailyWage;

    @Column(name = "piece_rate",precision = 19, scale = 2)
    private BigDecimal pieceRate;

    @Column(name = "hourly_rate",precision = 19, scale = 2)
    private BigDecimal hourlyRate;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
