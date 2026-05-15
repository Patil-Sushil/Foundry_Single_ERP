package com.kalibyte.foundry.labors.labor.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.labors.labor.dto.LaborerRequest;
import com.kalibyte.foundry.labors.labor.dto.LaborerResponse;
import com.kalibyte.foundry.labors.labor.entity.Laborer;
import com.kalibyte.foundry.labors.labor.exception.LaborException;
import com.kalibyte.foundry.labors.labor.mapper.LaborerMapper;
import com.kalibyte.foundry.labors.labor.repository.LaborerRepository;
import com.kalibyte.foundry.labors.labor.service.LaborerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LaborerServiceImpl implements LaborerService {

    private final LaborerRepository laborerRepository;
    private final LaborerMapper laborerMapper;

    @Override
    @Transactional
    public LaborerResponse createLaborer(LaborerRequest request) {
        validateLaborerRequest(request);
        Laborer laborer = laborerMapper.toEntity(request);
        return laborerMapper.toResponse(laborerRepository.save(laborer));
    }

    @Override
    public List<LaborerResponse> getAllLaborers() {
        return laborerMapper.toResponseDTOList(laborerRepository.findAll());
    }

    @Override
    public LaborerResponse getLaborerById(Long id) {
        Laborer laborer = laborerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Laborer not found with id: " + id));
        return laborerMapper.toResponse(laborer);
    }

    @Override
    @Transactional
    public LaborerResponse updateLaborer(Long id, LaborerRequest request) {
        validateLaborerRequest(request);
        Laborer laborer = laborerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Laborer not found with id: " + id));
        laborerMapper.updateEntityFromDto(request, laborer);
        return laborerMapper.toResponse(laborerRepository.save(laborer));
    }

    @Override
    @Transactional
    public LaborerResponse deleteLaborer(Long id) {
        Laborer laborer = laborerRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Labor with id not found"));
        laborer.setIsActive(!laborer.getIsActive());
        return laborerMapper.toResponse(laborerRepository.save(laborer));
    }

    private void validateLaborerRequest(LaborerRequest request) {
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
