package com.kalibyte.foundry.furnace.furnace_heats.entity;

import com.kalibyte.foundry.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FurnaceHeatsTest {

    @Test
    void testMeltingLossCalculation() {
        FurnaceHeats heat = FurnaceHeats.builder()
                .totalWeight(1000.0)
                .liquidMetalWeight(BigDecimal.valueOf(960))
                .slagWeight(BigDecimal.valueOf(20))
                .build();

        assertEquals(0, BigDecimal.valueOf(20).compareTo(heat.getMeltingLoss()));
        assertEquals(new BigDecimal("2.00"), heat.getMeltingLossPercentage());
    }

    @Test
    void testPouringLossCalculation() {
        FurnaceHeats heat = FurnaceHeats.builder()
                .liquidMetalWeight(BigDecimal.valueOf(960))
                .castingsPouredWeight(BigDecimal.valueOf(800))
                .runnerWeight(BigDecimal.valueOf(50))
                .riserWeight(BigDecimal.valueOf(50))
                .skullWeight(BigDecimal.valueOf(30))
                .spillageWeight(BigDecimal.valueOf(10))
                .build();

        assertEquals(0, BigDecimal.valueOf(20).compareTo(heat.getPouringLoss()));
        assertEquals(new BigDecimal("2.08"), heat.getPouringLossPercentage());
    }

    @Test
    void testValidateMetalBalance_Stage1_Failure() {
        FurnaceHeats heat = FurnaceHeats.builder()
                .totalWeight(1000.0)
                .liquidMetalWeight(BigDecimal.valueOf(990))
                .slagWeight(BigDecimal.valueOf(20))
                .build();

        BusinessException ex = assertThrows(BusinessException.class, heat::validateMetalBalance);
        assertTrue(ex.getMessage().contains("Liquid metal + slag (1010 kg) cannot exceed total charge weight (1000.0 kg)"));
    }

    @Test
    void testValidateMetalBalance_Stage2_Failure() {
        FurnaceHeats heat = FurnaceHeats.builder()
                .totalWeight(1000.0)
                .liquidMetalWeight(BigDecimal.valueOf(900))
                .slagWeight(BigDecimal.valueOf(20))
                .castingsPouredWeight(BigDecimal.valueOf(800))
                .runnerWeight(BigDecimal.valueOf(50))
                .riserWeight(BigDecimal.valueOf(50))
                .skullWeight(BigDecimal.valueOf(10))
                .spillageWeight(BigDecimal.valueOf(10))
                .build();
        
        BusinessException ex = assertThrows(BusinessException.class, heat::validateMetalBalance);
        assertTrue(ex.getMessage().contains("Metal balance exceeded! Liquid metal: 900 kg, but breakdown totals: 920 kg"));
    }

    @Test
    void testRecoverableScrap() {
        FurnaceHeats heat = FurnaceHeats.builder()
                .runnerWeight(BigDecimal.valueOf(50))
                .riserWeight(BigDecimal.valueOf(50))
                .skullWeight(BigDecimal.valueOf(10))
                .spillageWeight(BigDecimal.valueOf(5))
                .build();

        assertEquals(0, new BigDecimal("115").compareTo(heat.getRecoverableScrap()));
    }

    @Test
    void testBackwardCompatibility() {
        FurnaceHeats heat = FurnaceHeats.builder()
                .liquidMetalWeight(BigDecimal.valueOf(100))
                .castingsPouredWeight(BigDecimal.valueOf(90))
                .build();

        assertEquals(0, BigDecimal.valueOf(10).compareTo(heat.getMetalLoss()));
        assertEquals(0, heat.getPouringLoss().compareTo(heat.getMetalLoss()));
    }
}
