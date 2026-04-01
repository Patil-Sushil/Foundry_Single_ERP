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
        if (req == null || req.getLiquidMetalWeight() == null) return true; // Skip if not provided

        BigDecimal liquid = req.getLiquidMetalWeight();
        BigDecimal breakdown = safe(req.getCastingsPouredWeight())
                .add(safe(req.getRunnerWeight()))
                .add(safe(req.getRiserWeight()))
                .add(safe(req.getSkullWeight()))
                .add(safe(req.getSpillageWeight()));

        if (breakdown.compareTo(liquid) > 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format(
                            "Metal breakdown (%s kg) exceeds liquid metal weight (%s kg) " +
                                    "(Castings: %s + Runners: %s + Risers: %s + Skull: %s + Spillage: %s)",
                            breakdown, liquid,
                            safe(req.getCastingsPouredWeight()),
                            safe(req.getRunnerWeight()),
                            safe(req.getRiserWeight()),
                            safe(req.getSkullWeight()),
                            safe(req.getSpillageWeight()))
            ).addConstraintViolation();
            return false;
        }

        // Validate order items against castings poured
        if (req.getHeatOrderItems() != null && req.getCastingsPouredWeight() != null) {
            BigDecimal totalProduced = req.getHeatOrderItems().stream()
                    .map(item -> safe(item.getWeightProduced()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalProduced.compareTo(req.getCastingsPouredWeight()) > 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        String.format(
                                "Total weight produced (%s kg) exceeds " +
                                        "castings poured weight (%s kg)",
                                totalProduced, req.getCastingsPouredWeight())
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
