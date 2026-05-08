package com.kalibyte.foundry.furnace.furnace_heats.mapper;

import com.kalibyte.foundry.furnace.furnace_heats.dto.request.FurnaceHeatRequest;
import com.kalibyte.foundry.furnace.furnace_heats.dto.response.FurnaceHeatResponse;
import com.kalibyte.foundry.furnace.furnace_heats.dto.response.HeatMaterialItemResponse;
import com.kalibyte.foundry.furnace.furnace_heats.entity.Enum.HeatMaterialType;
import com.kalibyte.foundry.furnace.furnace_heats.entity.FurnaceHeats;
import com.kalibyte.foundry.furnace.furnace_heats.repository.ElectricityRateRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

@Mapper(componentModel = "spring", uses = {HeatMaterialMapper.class, HeatOrderItemMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class FurnaceHeatMapper {

    @Autowired
    protected ElectricityRateRepository electricityRateRepository;

    @Mapping(source = "order.id", target = "orderId")
    public abstract FurnaceHeatResponse toResponse(FurnaceHeats heat);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "materialsUsed", ignore = true)
    @Mapping(target = "heatOrderItems", ignore = true)
    @Mapping(target = "furnace", ignore = true)
    @Mapping(target = "id", ignore = true)
    public abstract FurnaceHeats toEntity(FurnaceHeatRequest request);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "materialsUsed", ignore = true)
    @Mapping(target = "heatOrderItems", ignore = true)
    @Mapping(target = "furnace", ignore = true)
    @Mapping(target = "id", ignore = true)
    public abstract void updateEntity(FurnaceHeatRequest request, @MappingTarget FurnaceHeats heat);

    @AfterMapping
    protected void calculateDerivedFields(FurnaceHeats entity, @MappingTarget FurnaceHeatResponse response) {
        // Melting stage fields
        response.setMeltingLoss(entity.getMeltingLoss());
        response.setMeltingLossPercentage(entity.getMeltingLossPercentage());

        if (entity.getLiquidMetalWeight() != null && entity.getLiquidMetalWeight().compareTo(BigDecimal.ZERO) > 0) {
            // Pouring stage fields
            response.setPouringLoss(entity.getPouringLoss());
            response.setPouringLossPercentage(entity.getPouringLossPercentage());
            
            // Backward compatibility
            response.setMetalLoss(entity.getPouringLoss());

            // Yield percentage = (castings / liquid metal) * 100
            BigDecimal castings = entity.getCastingsPouredWeight() != null
                    ? entity.getCastingsPouredWeight() : BigDecimal.ZERO;
            BigDecimal yield = castings
                    .multiply(BigDecimal.valueOf(100))
                    .divide(entity.getLiquidMetalWeight(), 2, java.math.RoundingMode.HALF_UP);
            response.setYieldPercentage(yield);

            // Remaining capacity
            response.setRemainingCapacity(entity.getRemainingCastingsCapacity());
        }

        // Add virtual electricity material item to the response
        addElectricityMaterialToResponse(entity, response);
    }

    private void addElectricityMaterialToResponse(FurnaceHeats entity, FurnaceHeatResponse response) {
        double unitsConsumed = entity.getDifferenceReading();
        if (unitsConsumed <= 0) return;

        LocalDate heatDate = (entity.getFurnace() != null) ? entity.getFurnace().getDate() : LocalDate.now();

        double rate = electricityRateRepository.findRatesEffectiveOn(heatDate).stream().findFirst()
                .or(() -> electricityRateRepository.findFirstByEffectiveFromGreaterThanOrderByEffectiveFromAsc(heatDate))
                .or(() -> electricityRateRepository.findFirstByEffectiveFromLessThanOrderByEffectiveFromDesc(heatDate))
                .or(() -> electricityRateRepository.findFirstByActiveTrueOrderByIdDesc())
                .map(er -> er.getRatePerUnit())
                .orElse(0.0);

        HeatMaterialItemResponse electricityItem = HeatMaterialItemResponse.builder()
                .id(null)
                .itemId(null)
                .itemName("Electricity Consumed")
                .materialType(HeatMaterialType.ELECTRICITY)
                .quantityUsed(unitsConsumed)
                .unitRate(rate)
                .totalCost(unitsConsumed * rate)
                .build();

        if (response.getMaterialsUsed() == null) {
            response.setMaterialsUsed(new ArrayList<>());
        } else {
            // Ensure we don't duplicate if called multiple times in some edge cases
            response.getMaterialsUsed().removeIf(m -> m.getMaterialType() == HeatMaterialType.ELECTRICITY);
        }
        response.getMaterialsUsed().add(electricityItem);
    }
}
