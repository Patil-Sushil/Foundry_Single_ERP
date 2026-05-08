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

    /**
     * Weight of slag/impurity waste removed during melting.
     * Slag is NOT recoverable scrap and is considered a non-recoverable loss.
     */
    @Builder.Default
    @Column(name = "slag_weight")
    private BigDecimal slagWeight = BigDecimal.ZERO;

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
     * Validates the metal balance in two stages:
     *
     * STAGE 1: MELTING BALANCE
     * Charge Weight >= Liquid Metal + Slag
     * (Melting Loss is the difference)
     *
     * STAGE 2: POURING BALANCE
     * Liquid Metal >= Castings + Runner + Riser + Skull + Spillage
     * (Pouring Loss is the difference)
     *
     * AND:
     * sum(heatOrderItems.weightProduced) == castingsPouredWeight
     */
    public void validateMetalBalance() {
        BigDecimal liquid = safeValue(liquidMetalWeight);
        BigDecimal slag = safeValue(slagWeight);
        BigDecimal chargeWeight = BigDecimal.valueOf(totalWeight);

        // STAGE 1: MELTING BALANCE
        // Validate: Liquid metal + slag (outputs) cannot exceed Total Weight (input charge)
        if (chargeWeight.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalMeltingOutput = liquid.add(slag);
            if (totalMeltingOutput.compareTo(chargeWeight) > 0) {
                throw new com.kalibyte.foundry.common.exception.BusinessException(String.format(
                        "Liquid metal + slag (%s kg) cannot exceed total charge weight (%s kg).",
                        totalMeltingOutput, chargeWeight));
            }
        }

        if (liquid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new com.kalibyte.foundry.common.exception.BusinessException("Liquid metal weight must be greater than zero.");
        }

        // STAGE 2: POURING BALANCE
        // Validate: breakdown must not exceed liquid metal
        BigDecimal totalBreakdown = safeValue(castingsPouredWeight)
                .add(safeValue(runnerWeight))
                .add(safeValue(riserWeight))
                .add(safeValue(skullWeight))
                .add(safeValue(spillageWeight));

        if (totalBreakdown.compareTo(liquid) > 0) {
            throw new com.kalibyte.foundry.common.exception.BusinessException(String.format(
                    "Metal balance exceeded! Liquid metal: %s kg, " +
                            "but breakdown totals: %s kg " +
                            "(Castings: %s + Runners: %s + Risers: %s + Skull: %s + Spillage: %s)",
                    liquid, totalBreakdown,
                    safeValue(castingsPouredWeight),
                    safeValue(runnerWeight),
                    safeValue(riserWeight),
                    safeValue(skullWeight),
                    safeValue(spillageWeight)
            ));
        }

        // 3. Validate: total weight produced across order items
        //    must exactly match castings poured weight
        BigDecimal castingsPoured = safeValue(castingsPouredWeight);
        if (castingsPoured.compareTo(BigDecimal.ZERO) > 0) {
            if (heatOrderItems == null || heatOrderItems.isEmpty()) {
                throw new com.kalibyte.foundry.common.exception.BusinessException(
                        "Castings were poured but no produced items were recorded. " +
                        "Please allocate the poured weight to specific order items or stock items.");
            }

            BigDecimal totalWeightProduced = heatOrderItems.stream()
                    .map(item -> safeValue(item.getWeightProduced()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalWeightProduced.compareTo(castingsPoured) != 0) {
                throw new com.kalibyte.foundry.common.exception.BusinessException(String.format(
                        "Total weight produced by order/stock items (%s kg) must exactly match " +
                                "castings poured weight (%s kg).",
                        totalWeightProduced, castingsPoured
                ));
            }
        }
    }

    /**
     * Calculates Melting Loss.
     * Formula: Total Charge Weight - Liquid Metal Weight - Slag Weight
     * Represents losses due to oxidation, moisture, and fine dust during the melting stage.
     */
    public BigDecimal getMeltingLoss() {
        BigDecimal chargeWeight = BigDecimal.valueOf(totalWeight);
        if (chargeWeight.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal loss = chargeWeight
                .subtract(safeValue(liquidMetalWeight))
                .subtract(safeValue(slagWeight));

        return loss.compareTo(BigDecimal.ZERO) > 0 ? loss : BigDecimal.ZERO;
    }

    /**
     * Calculates Pouring Loss (formerly metalLoss).
     * Formula: Liquid Metal Weight - (Castings + Runner + Riser + Skull + Spillage)
     * Represents unaccounted pouring/process loss, hidden weighing inaccuracies, 
     * or oxidation during pouring.
     */
    public BigDecimal getPouringLoss() {
        if (liquidMetalWeight == null) return BigDecimal.ZERO;

        BigDecimal totalBreakdown = safeValue(castingsPouredWeight)
                .add(safeValue(runnerWeight))
                .add(safeValue(riserWeight))
                .add(safeValue(skullWeight))
                .add(safeValue(spillageWeight));

        BigDecimal loss = liquidMetalWeight.subtract(totalBreakdown);
        return loss.compareTo(BigDecimal.ZERO) > 0 ? loss : BigDecimal.ZERO;
    }

    /**
     * Alias for getPouringLoss to maintain backward compatibility with existing APIs.
     * @deprecated Use getPouringLoss() for clearer intent.
     */
    @Deprecated
    public BigDecimal getMetalLoss() {
        return getPouringLoss();
    }

    /**
     * Calculates Melting Loss Percentage.
     * Formula: (Melting Loss / Total Charge Weight) * 100
     */
    @Transient
    public BigDecimal getMeltingLossPercentage() {
        BigDecimal chargeWeight = BigDecimal.valueOf(totalWeight);
        if (chargeWeight.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        return getMeltingLoss()
                .multiply(BigDecimal.valueOf(100))
                .divide(chargeWeight, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculates Pouring Loss Percentage.
     * Formula: (Pouring Loss / Liquid Metal Weight) * 100
     */
    @Transient
    public BigDecimal getPouringLossPercentage() {
        BigDecimal liquid = safeValue(liquidMetalWeight);
        if (liquid.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        return getPouringLoss()
                .multiply(BigDecimal.valueOf(100))
                .divide(liquid, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Returns total recoverable/remeltable process scrap.
     * Formula: Runner + Riser + Skull + Spillage
     * Slag is NOT included as it is metallurgical waste.
     */
    @Transient
    public BigDecimal getRecoverableScrap() {
        return safeValue(runnerWeight)
                .add(safeValue(riserWeight))
                .add(safeValue(skullWeight))
                .add(safeValue(spillageWeight));
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
