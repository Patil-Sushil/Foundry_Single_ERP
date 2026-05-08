package com.kalibyte.foundry.furnace.furnace_heats.validation;

import com.kalibyte.foundry.furnace.furnace_heats.dto.request.FurnaceHeatRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class MetalBalanceValidator
        implements ConstraintValidator<ValidMetalBalance, FurnaceHeatRequest> {

    @Override
    public boolean isValid(FurnaceHeatRequest req,
                           ConstraintValidatorContext context) {
        if (req == null) return true;

        BigDecimal liquid = safe(req.getLiquidMetalWeight());
        BigDecimal slag = safe(req.getSlagWeight());
        BigDecimal chargeWeight = req.getTotalWeight() != null ? BigDecimal.valueOf(req.getTotalWeight()) : BigDecimal.ZERO;

        // 1. STAGE 1: MELTING BALANCE
        // Validate: Liquid metal + slag (outputs) cannot exceed Total Weight (input charge)
        if (chargeWeight.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalMeltingOutput = liquid.add(slag);
            if (totalMeltingOutput.compareTo(chargeWeight) > 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        String.format("Liquid metal + slag (%s kg) cannot exceed total charge weight (%s kg)",
                                totalMeltingOutput, chargeWeight)
                ).addConstraintViolation();
                return false;
            }
        }

        if (liquid.compareTo(BigDecimal.ZERO) <= 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Liquid metal weight must be greater than zero")
                    .addConstraintViolation();
            return false;
        }

        // 2. STAGE 2: POURING BALANCE
        // Validate: Metal breakdown vs Liquid Metal
        BigDecimal breakdown = safe(req.getCastingsPouredWeight())
                .add(safe(req.getRunnerWeight()))
                .add(safe(req.getRiserWeight()))
                .add(safe(req.getSkullWeight()))
                .add(safe(req.getSpillageWeight()));

        if (breakdown.compareTo(liquid) > 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format(
                            "Metal balance exceeded! Liquid metal: %s kg, " +
                                    "but breakdown totals: %s kg " +
                                    "(Castings: %s + Runners: %s + Risers: %s + Skull: %s + Spillage: %s)",
                            liquid, breakdown,
                            safe(req.getCastingsPouredWeight()),
                            safe(req.getRunnerWeight()),
                            safe(req.getRiserWeight()),
                            safe(req.getSkullWeight()),
                            safe(req.getSpillageWeight()))
            ).addConstraintViolation();
            return false;
        }

        // 3. Validate: Castings Poured vs Produced Items
        BigDecimal castingsPoured = safe(req.getCastingsPouredWeight());
        if (castingsPoured.compareTo(BigDecimal.ZERO) > 0) {
            if (req.getHeatOrderItems() == null || req.getHeatOrderItems().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Castings were poured but no items (orders or stock) were recorded. " +
                        "Please allocate the poured weight to specific items."
                ).addConstraintViolation();
                return false;
            }

            BigDecimal totalProduced = req.getHeatOrderItems().stream()
                    .map(item -> safe(item.getWeightProduced()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalProduced.compareTo(castingsPoured) != 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        String.format(
                                "Total weight produced (%s kg) must exactly match " +
                                        "castings poured weight (%s kg).",
                                totalProduced, castingsPoured)
                ).addConstraintViolation();
                return false;
            }
        }

        return true;
    }

    private BigDecimal safe(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
