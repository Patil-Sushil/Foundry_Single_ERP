package com.kalibyte.foundry.furnace.furnace_report.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.furnace.furnace_heats.dto.request.FurnaceHeatRequest;
import com.kalibyte.foundry.furnace.furnace_heats.entity.Enum.HeatMaterialType;
import com.kalibyte.foundry.furnace.furnace_heats.service.FurnaceHeatService;
import com.kalibyte.foundry.furnace.furnace_report.common.FurnaceRefNoGenerator;
import com.kalibyte.foundry.furnace.furnace_report.dto.Request.FurnaceRequestDTO;
import com.kalibyte.foundry.furnace.furnace_report.dto.response.FurnaceResponseDTO;
import com.kalibyte.foundry.furnace.furnace_report.entity.Furnace;
import com.kalibyte.foundry.furnace.furnace_report.repository.FurnaceRepository;
import com.kalibyte.foundry.furnace.furnace_report.service.FurnaceService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FurnaceServiceImpl implements FurnaceService {

	private final FurnaceRepository furnaceRepository;
	private final ModelMapper modelMapper;
	private final FurnaceRefNoGenerator furnaceRefNoGenerator;
	private final FurnaceHeatService furnaceHeatService;

	public FurnaceServiceImpl(FurnaceRepository furnaceRepository, ModelMapper modelMapper, FurnaceRefNoGenerator furnaceRefNoGenerator, FurnaceHeatService furnaceHeatService) {
		this.furnaceRepository = furnaceRepository;
		this.modelMapper = modelMapper;
		this.furnaceRefNoGenerator = furnaceRefNoGenerator;
		this.furnaceHeatService = furnaceHeatService;
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
	@Transactional(readOnly = true)
	public FurnaceResponseDTO findById(long id) {
		return furnaceRepository.findById(id)
				.map(furnace -> modelMapper.map(furnace, FurnaceResponseDTO.class))
				.orElseThrow(() -> new ResourceNotFoundException("Furnace report not found with id: " + id));
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
	@Transactional
	public FurnaceResponseDTO createFurnace(@Valid FurnaceRequestDTO request) {
		Furnace furnace = new Furnace();
		furnace.setOperatorName(request.getOperatorName());
		furnace.setShift(request.getShift());
		furnace.setInchargeName(request.getInchargeName());
		furnace.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());
		furnace.setFurnaceRefNo(furnaceRefNoGenerator.generate());

		Furnace savedFurnace = furnaceRepository.save(furnace);

		// Create heats through FurnaceHeatService to ensure proper material issuance
		if (request.getHeats() != null) {
			for (FurnaceHeatRequest heatRequest : request.getHeats()) {
				furnaceHeatService.createHeat(savedFurnace.getId(), heatRequest);
			}
		}

		return modelMapper.map(furnaceRepository.findById(savedFurnace.getId()).get(), FurnaceResponseDTO.class);
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
	@Transactional
	public FurnaceResponseDTO updateFurnace(Long id, FurnaceRequestDTO request) {
		Furnace existingFurnace = furnaceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Furnace report not found with id: " + id));

		// Update basic fields
		existingFurnace.setOperatorName(request.getOperatorName());
		existingFurnace.setShift(request.getShift());
		existingFurnace.setInchargeName(request.getInchargeName());
		existingFurnace.setDate(request.getDate());

		// Update heats - Delegate to FurnaceHeatService
		// For a full report update, we could delete existing and re-add or use delta logic.
		// Given updateFurnace in this service was already reversing all and re-adding, we'll follow that pattern.
		furnaceHeatService.deleteAllHeatsByReportId(id);
		
		if (request.getHeats() != null) {
			for (FurnaceHeatRequest heatRequest : request.getHeats()) {
				furnaceHeatService.createHeat(id, heatRequest);
			}
		}

		Furnace savedFurnace = furnaceRepository.save(existingFurnace);
		return modelMapper.map(savedFurnace, FurnaceResponseDTO.class);
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
	@Transactional
	public void deleteFurnace(Long id) {
		Furnace furnace = furnaceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Furnace report not found with id: " + id));
		
		furnaceHeatService.deleteAllHeatsByReportId(id);
		furnaceRepository.delete(furnace);
	}

	@PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
	public List<Map<String, Object>> getMaterialSummary(Long reportId, HeatMaterialType type) {
		// This logic can stay here as it aggregates across heats, 
		// or it could be moved to FurnaceHeatService if preferred.
		// I'll keep it here but refactor to use repository if I had it, 
		// but I can still access furnace.getHeats() as it's a bidirectional relationship.
		
		Furnace furnace = furnaceRepository.findById(reportId)
				.orElseThrow(() -> new ResourceNotFoundException("Furnace report not found with id: " + reportId));

		// Same implementation as before, accessing nested heats/materials
		Map<String, Map<String, Object>> summaryMap = new java.util.HashMap<>();

		furnace.getHeats().forEach(heat -> {
			if (heat.getMaterialsUsed() == null) return;
			heat.getMaterialsUsed().forEach(material -> {
				if (type != null && material.getMaterialType() != type) return;

				String key = material.getItemId() + "_" + material.getMaterialType();
				Map<String, Object> summary = summaryMap.computeIfAbsent(key, k -> {
					Map<String, Object> map = new java.util.HashMap<>();
					map.put("itemId", material.getItemId());
					map.put("itemName", material.getItemName());
					map.put("materialType", material.getMaterialType());
					map.put("totalQuantityUsed", 0.0);
					map.put("totalCost", 0.0);
					return map;
				});

				summary.put("totalQuantityUsed", (Double) summary.get("totalQuantityUsed") + material.getQuantityUsed());
				summary.put("totalCost", (Double) summary.get("totalCost") + material.getTotalCost());
			});
		});

		summaryMap.values().forEach(summary -> {
			double totalQty = (Double) summary.get("totalQuantityUsed");
			double totalCost = (Double) summary.get("totalCost");
			summary.put("avgUnitRate", totalQty > 0 ? totalCost / totalQty : 0.0);
		});

		return new java.util.ArrayList<>(summaryMap.values());
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
	@Transactional(readOnly = true)
	public FurnaceResponseDTO findByFurnaceRefNo(String refNo) {
		return furnaceRepository.findByFurnaceRefNo(refNo)
				.map(furnace -> modelMapper.map(furnace, FurnaceResponseDTO.class))
				.orElseThrow(() -> new ResourceNotFoundException("Furnace report not found with refNo: " + refNo));
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
	@Transactional(readOnly = true)
	public List<FurnaceResponseDTO> findAll() {
		return furnaceRepository.findAllWithHeats().stream()
				.map(furnace -> modelMapper.map(furnace, FurnaceResponseDTO.class)).collect(Collectors.toList());
	}
}
