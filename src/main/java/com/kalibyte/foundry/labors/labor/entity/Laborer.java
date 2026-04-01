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

    @Column(name = "wage_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private WageType wageType; // 'HOURLY' or 'PIECE_RATE'

    @Column(name = "daily_wage",precision = 19, scale = 2)
    private BigDecimal dailyWage;

    @Column(name = "piece_rate",precision = 19, scale = 2)
    private BigDecimal pieceRate;

    @Column(name = "hourly_rate",precision = 19, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
