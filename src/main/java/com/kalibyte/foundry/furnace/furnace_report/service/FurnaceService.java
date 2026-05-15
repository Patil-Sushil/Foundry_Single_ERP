package com.kalibyte.foundry.furnace.furnace_report.service;

import com.kalibyte.foundry.furnace.furnace_report.dto.response.FurnaceResponse;
import com.kalibyte.foundry.furnace.furnace_report.dto.Request.FurnaceRequest;

import com.kalibyte.foundry.furnace.furnace_heats.entity.Enum.HeatMaterialType;
import java.util.List;
import java.util.Map;

public interface FurnaceService {
	FurnaceResponse findById(long id);
	FurnaceResponse createFurnace(FurnaceRequest request);
	FurnaceResponse findByFurnaceRefNo(String refNo);
	List<FurnaceResponse> findAll();
	FurnaceResponse updateFurnace(Long id, FurnaceRequest request);
	void deleteFurnace(Long id);
	List<Map<String, Object>> getMaterialSummary(Long reportId, HeatMaterialType type);
}
