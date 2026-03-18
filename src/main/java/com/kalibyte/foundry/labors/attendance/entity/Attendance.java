package com.kalibyte.foundry.labors.attendance.entity;

import com.kalibyte.foundry.labors.labor.entity.Enum.WageType;
import com.kalibyte.foundry.labors.labor.entity.Laborer;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"laborer_id", "work_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laborer_id", nullable = false)
    private Laborer laborer;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @Column(name = "hours_worked", precision = 10, scale = 2)
    private BigDecimal hoursWorked;

    @Column(name = "pieces_completed")
    private Integer piecesCompleted;

    @Column(name = "earned_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal earnedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "wage_type_snapshot", nullable = false)
    private WageType wageTypeSnapshot;

    @Column(name = "applied_rate", nullable = false, precision = 19, scale = 2)
    private BigDecimal appliedRate;  // the actual rate used for this calculation
}
