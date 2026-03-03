package com.kalibyte.foundry.furnace.furnace_report.service;

import com.kalibyte.foundry.furnace.furnace_report.dto.response.FurnaceResponseDTO;
import com.kalibyte.foundry.furnace.furnace_report.dto.Request.FurnaceRequestDTO;

import com.kalibyte.foundry.furnace.furnace_heats.entity.HeatMaterialType;
import java.util.List;
import java.util.Map;

public interface FurnaceService {
	FurnaceResponseDTO findById(long id);
	FurnaceResponseDTO createFurnace(FurnaceRequestDTO request);
	FurnaceResponseDTO findByFurnaceRefNo(String refNo);
	List<FurnaceResponseDTO> findAll();
	FurnaceResponseDTO updateFurnace(Long id, FurnaceRequestDTO request);
	void deleteFurnace(Long id);
	List<Map<String, Object>> getMaterialSummary(Long reportId, HeatMaterialType type);
}
