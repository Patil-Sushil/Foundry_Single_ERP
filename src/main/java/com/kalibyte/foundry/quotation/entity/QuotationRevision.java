package com.kalibyte.foundry.quotation.entity;
import com.kalibyte.foundry.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "quotation_revisions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"quotation_id", "revision_no"}))
public class QuotationRevision extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @Column(name = "revision_no", nullable = false)
    private Integer revisionNo;

    @Column(name = "revision_date", nullable = false)
    private LocalDate revisionDate;

    @Column(length = 500)
    private String reason;

    @Column(name = "previous_total", precision = 19, scale = 2)
    private BigDecimal previousTotal;

    @Column(name = "revised_total", precision = 19, scale = 2)
    private BigDecimal revisedTotal;

    @Column(name = "changed_by")
    private String changedBy;

    //  Calculate difference
    @Transient
    public BigDecimal getDifference() {
        if (revisedTotal != null && previousTotal != null) {
            return revisedTotal.subtract(previousTotal);
        }
        return BigDecimal.ZERO;
    }

    //  Calculate percentage change
    @Transient
    public BigDecimal getPercentageChange() {
        if (previousTotal != null && previousTotal.compareTo(BigDecimal.ZERO) != 0) {
            return getDifference()
                    .divide(previousTotal, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}