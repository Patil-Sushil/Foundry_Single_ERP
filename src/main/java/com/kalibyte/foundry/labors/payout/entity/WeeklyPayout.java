package com.kalibyte.foundry.labors.payout.entity;

import com.kalibyte.foundry.labors.labor.entity.Laborer;
import com.kalibyte.foundry.labors.payout.entity.Enum.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "weekly_payouts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"laborer_id", "week_start_date", "week_end_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laborer_id", nullable = false)
    private Laborer laborer;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Column(name = "total_hours", precision = 10, scale = 2)
    private BigDecimal totalHours;

    @Column(name = "gross_payout", nullable = false, precision = 19, scale = 2)
    private BigDecimal grossPayout;

    @Column(name = "advance_deduction", nullable = false, precision = 19, scale = 2)
    private BigDecimal advanceDeduction;

    @Column(name = "net_payout", nullable = false, precision = 19, scale = 2)
    private BigDecimal netPayout;

    @Column(name = "payment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus; // 'PENDING' or 'PAID'

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "pieces_completed")
    private BigDecimal piecesCompleted;
}
