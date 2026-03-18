package com.kalibyte.foundry.labors.labor.service;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.labors.labor.dto.LaborerRequestDTO;
import com.kalibyte.foundry.labors.labor.dto.LaborerResponseDTO;
import com.kalibyte.foundry.labors.labor.entity.Laborer;
import com.kalibyte.foundry.labors.labor.mapper.LaborerMapper;
import com.kalibyte.foundry.labors.labor.repository.LaborerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LaborerService {

    private final LaborerRepository laborerRepository;
    private final LaborerMapper laborerMapper;

	public LaborerService(LaborerRepository laborerRepository, LaborerMapper laborerMapper) {
		this.laborerRepository = laborerRepository;
		this.laborerMapper = laborerMapper;
	}


	@Transactional
    public LaborerResponseDTO createLaborer(LaborerRequestDTO request) {
        Laborer laborer = laborerMapper.toEntity(request);
        return laborerMapper.toResponse(laborerRepository.save(laborer));

    }

    public List<LaborerResponseDTO> getAllLaborers() {
        return laborerMapper.toResponseDTOList(laborerRepository.findAll());
    }

    public LaborerResponseDTO getLaborerById(Long id) {
        Laborer laborer = laborerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Laborer not found with id: " + id));
        return laborerMapper.toResponse(laborer);
    }

	public LaborerResponseDTO deleteLaborer(Long id) {
		Laborer laborer = laborerRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Labor with id not found"));
		laborer.setIsActive(!laborer.getIsActive());
		return laborerMapper.toResponse(laborerRepository.save(laborer));
	}
}
