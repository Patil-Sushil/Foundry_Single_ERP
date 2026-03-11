package com.kalibyte.foundry.furnace.furnace_heats.service;

import com.kalibyte.foundry.furnace.furnace_heats.dto.FurnaceHeatRequest;
import com.kalibyte.foundry.furnace.furnace_heats.dto.FurnaceHeatResponse;
import com.kalibyte.foundry.furnace.furnace_heats.dto.HeatsByOrderResponse;

import java.util.List;
import java.util.UUID;

public interface FurnaceHeatService {
    List<FurnaceHeatResponse> getHeatsByReportId(Long reportId);
    FurnaceHeatResponse getHeatById(Long heatId);
    FurnaceHeatResponse createHeat(Long reportId, FurnaceHeatRequest request);
    FurnaceHeatResponse updateHeat(Long heatId, FurnaceHeatRequest request);
    HeatsByOrderResponse getHeatsByOrderId(UUID orderId);
    void deleteHeat(Long heatId);
    void deleteAllHeatsByReportId(Long reportId);
}
