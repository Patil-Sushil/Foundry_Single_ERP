package com.kalibyte.foundry.furnace.furnace_heats.entity;

import com.kalibyte.foundry.furnace.furnace_report.entity.Furnace;
import com.kalibyte.foundry.order.entity.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.generator.EventType.INSERT;

@Entity
@Getter
@Setter
@Table(name="furnace_heats")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FurnaceHeats {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private double sipercentage;

	private double cpcpercentage;

	private double mgpercentage;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "furnace_id")
	private Furnace furnace;

	private double startReading;

	private double stopReading;

	private double differenceReading;

	private double totalWeight;

	private double pouringTemp;

	private double powerToWeight;

	@OneToMany(mappedBy = "heat", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<HeatMaterialItem> materialsUsed;

	private LocalTime pouringStartTime;

	private LocalTime pouringEndTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

    @Column(nullable = false)
    private String grade;

    @Column(name = "liquid_metal_weight")
    private BigDecimal liquidMetalWeight;

    @Column(name = "castings_poured_weight")
    private BigDecimal castingsPouredWeight;

    @Builder.Default
    @Column(name = "runner_weight")
    private BigDecimal runnerWeight = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "riser_weight")
    private BigDecimal riserWeight = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "skull_weight")
    private BigDecimal skullWeight = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "spillage_weight")
    private BigDecimal spillageWeight = BigDecimal.ZERO;

    @Generated(event = INSERT)
    @Column(name = "total_process_scrap", insertable = false, updatable = false)
    private BigDecimal totalProcessScrap;

    @Column(name = "process_scrap_entry_id")
    private Long processScrapEntryId;

    @Builder.Default
    @Column(name = "auto_return_scrap")
    private Boolean autoReturnScrap = true;

    @Generated(event = INSERT)
    @Column(name = "furnace_yield_percentage", insertable = false, updatable = false)
    private BigDecimal furnaceYieldPercentage;

    @Generated(event = INSERT)
    @Column(name = "pouring_yield_percentage", insertable = false, updatable = false)
    private BigDecimal pouringYieldPercentage;

    @Builder.Default
    @OneToMany(mappedBy = "heat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HeatOrderItem> heatOrderItems = new ArrayList<>();

    /**
     * Validates the metal balance:
     *
     * liquidMetalWeight >= castingsPouredWeight + runnerWeight
     *                      + riserWeight + skullWeight + spillageWeight
     *
     * And:
     * sum(heatOrderItems.weightProduced) <= castingsPouredWeight
     */
    public void validateMetalBalance() {
        if (liquidMetalWeight == null || liquidMetalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new com.kalibyte.foundry.common.exception.BusinessException("Liquid metal weight must be greater than zero.");
        }

        // 1. Validate: breakdown must not exceed liquid metal
        BigDecimal totalBreakdown = safeValue(castingsPouredWeight)
                .add(safeValue(runnerWeight))
                .add(safeValue(riserWeight))
                .add(safeValue(skullWeight))
                .add(safeValue(spillageWeight));

        if (totalBreakdown.compareTo(liquidMetalWeight) > 0) {
            throw new com.kalibyte.foundry.common.exception.BusinessException(String.format(
                    "Metal balance exceeded! Liquid metal: %s kg, " +
                            "but breakdown totals: %s kg " +
                            "(Castings: %s + Runners: %s + Risers: %s + Skull: %s + Spillage: %s)",
                    liquidMetalWeight, totalBreakdown,
                    safeValue(castingsPouredWeight),
                    safeValue(runnerWeight),
                    safeValue(riserWeight),
                    safeValue(skullWeight),
                    safeValue(spillageWeight)
            ));
        }

        // 2. Validate: total weight produced across order items
        //    must not exceed castings poured weight
        if (heatOrderItems != null && !heatOrderItems.isEmpty()
                && castingsPouredWeight != null) {
            BigDecimal totalWeightProduced = heatOrderItems.stream()
                    .map(item -> safeValue(item.getWeightProduced()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalWeightProduced.compareTo(castingsPouredWeight) > 0) {
                throw new com.kalibyte.foundry.common.exception.BusinessException(String.format(
                        "Total weight produced by order items (%s kg) exceeds " +
                                "castings poured weight (%s kg).",
                        totalWeightProduced, castingsPouredWeight
                ));
            }
        }
    }

    /**
     * Returns the unaccounted metal (potential additional loss or measurement error).
     * liquidMetalWeight - (castings + runners + risers + skull + spillage)
     */
    public BigDecimal getMetalLoss() {
        if (liquidMetalWeight == null) return BigDecimal.ZERO;

        BigDecimal totalBreakdown = safeValue(castingsPouredWeight)
                .add(safeValue(runnerWeight))
                .add(safeValue(riserWeight))
                .add(safeValue(skullWeight))
                .add(safeValue(spillageWeight));

        return liquidMetalWeight.subtract(totalBreakdown);
    }

    /**
     * Returns remaining castings weight available for allocation to order items.
     */
    public BigDecimal getRemainingCastingsCapacity() {
        BigDecimal allocated = BigDecimal.ZERO;
        if (heatOrderItems != null) {
            allocated = heatOrderItems.stream()
                    .map(item -> safeValue(item.getWeightProduced()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return safeValue(castingsPouredWeight).subtract(allocated);
    }

    private BigDecimal safeValue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

	public void addMaterial(HeatMaterialItem material) {
		if (materialsUsed == null) {
			materialsUsed = new ArrayList<>();
		}
		materialsUsed.add(material);
		material.setHeat(this);
	}

	public void removeMaterial(HeatMaterialItem material) {
		if (materialsUsed != null) {
			materialsUsed.remove(material);
			material.setHeat(null);
		}
	}

    public void addHeatOrderItem(HeatOrderItem item) {
        if (heatOrderItems == null) {
            heatOrderItems = new ArrayList<>();
        }
        heatOrderItems.add(item);
        item.setHeat(this);
    }

    public void removeHeatOrderItem(HeatOrderItem item) {
        if (heatOrderItems != null) {
            heatOrderItems.remove(item);
            item.setHeat(null);
        }
    }
}
