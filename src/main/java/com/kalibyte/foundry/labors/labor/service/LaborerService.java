package com.kalibyte.foundry.labors.labor.service;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.labors.labor.dto.LaborerRequestDTO;
import com.kalibyte.foundry.labors.labor.dto.LaborerResponseDTO;
import com.kalibyte.foundry.labors.labor.entity.Laborer;
import com.kalibyte.foundry.labors.labor.exception.LaborException;
import com.kalibyte.foundry.labors.labor.mapper.LaborerMapper;
import com.kalibyte.foundry.labors.labor.repository.LaborerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
        validateLaborerRequest(request);
        Laborer laborer = laborerMapper.toEntity(request);
        return laborerMapper.toResponse(laborerRepository.save(laborer));

    }

    public List<LaborerResponseDTO> getAllLaborers() {
        return laborerMapper.toResponseDTOList(laborerRepository.findAll());
    }

    public LaborerResponseDTO getLaborerById(Long id) {
        Laborer laborer = laborerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Laborer not found with id: " + id));
        return laborerMapper.toResponse(laborer);
    }

    @Transactional
    public LaborerResponseDTO updateLaborer(Long id, LaborerRequestDTO request) {
        validateLaborerRequest(request);
        Laborer laborer = laborerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Laborer not found with id: " + id));
        laborerMapper.updateEntityFromDto(request, laborer);
        return laborerMapper.toResponse(laborerRepository.save(laborer));
    }

	public LaborerResponseDTO deleteLaborer(Long id) {
		Laborer laborer = laborerRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Labor with id not found"));
		laborer.setIsActive(!laborer.getIsActive());
		return laborerMapper.toResponse(laborerRepository.save(laborer));
	}

    private void validateLaborerRequest(LaborerRequestDTO request) {
        if (request.getPhNumber() == null || request.getPhNumber().length() != 10) {
            throw new LaborException("Phone number must be of 10 length");
        }
        if (request.getWageType() == null) {
            throw new LaborException("Wage type is required");
        }
        switch (request.getWageType()) {
            case HOURLY -> {
                if (request.getHourlyRate() == null || request.getHourlyRate().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new LaborException("Hourly rate must be present and greater than zero for HOURLY wage type");
                }
            }
            case DAILY -> {
                if (request.getDailyWage() == null || request.getDailyWage().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new LaborException("Daily wage must be present and greater than zero for DAILY wage type");
                }
            }
            case PIECE_RATE -> {
                if (request.getPieceRate() == null || request.getPieceRate().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new LaborException("Piece rate must be present and greater than zero for PIECE_RATE wage type");
                }
            }
        }
    }
}

