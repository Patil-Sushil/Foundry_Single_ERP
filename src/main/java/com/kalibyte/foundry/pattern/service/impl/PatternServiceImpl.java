package com.kalibyte.foundry.pattern.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.util.SecurityUtils;
import com.kalibyte.foundry.pattern.dto.request.PatternCreateRequest;
import com.kalibyte.foundry.pattern.dto.request.PatternStatusUpdateRequest;
import com.kalibyte.foundry.pattern.dto.request.PatternUpdateRequest;
import com.kalibyte.foundry.pattern.dto.response.PatternResponse;
import com.kalibyte.foundry.pattern.entity.enums.PatternStatus;
import com.kalibyte.foundry.pattern.entity.Pattern;
import com.kalibyte.foundry.pattern.mapper.PatternMapper;
import com.kalibyte.foundry.pattern.repository.PatternRepository;
import com.kalibyte.foundry.pattern.service.PatternService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatternServiceImpl implements PatternService {

    private final PatternRepository patternRepository;
    private final PatternMapper patternMapper;

    @Override
    @Transactional
    public PatternResponse create(PatternCreateRequest request) {

        Long sequence = patternRepository.getNextPatternSequence();

        String patternNumber = "PAT-" + String.format("%04d", sequence);

        Pattern pattern = Pattern.builder()
                .patternNumber(patternNumber)
                .patternName(request.getName())
                .type(request.getType())
                .material(request.getMaterial())
                .rackNumber(request.getRackNumber())
                .build();

        pattern.setCreatedBy(SecurityUtils.getCurrentUsername());

        patternRepository.save(pattern);

        return patternMapper.toResponse(pattern);
    }

    @Override
    public PageResponse<PatternResponse> getAll(int page, int size, String sort) {

        Sort sorting = Sort.by("createdAt").descending();

        if (sort != null && !sort.isBlank()) {

            String[] parts = sort.split(",");

            String field = parts[0];
            Sort.Direction direction = Sort.Direction.ASC;

            if (parts.length > 1 && parts[1].equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            }

            sorting = Sort.by(direction, field);
        }

        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Pattern> patternPage = patternRepository.findAll(pageable);

        return PageResponse.from(patternPage, patternMapper::toResponse);
    }

    @Override
    public PatternResponse getById(UUID id) {
        Pattern pattern = patternRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Pattern not found"));
        return patternMapper.toResponse(pattern);
    }

    @Override
    @Transactional
    public PatternResponse update(UUID id, PatternUpdateRequest request) {
        Pattern pattern =  patternRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Pattern not found"));

        pattern.setPatternName(request.getName());
        pattern.setType(request.getType());
        pattern.setMaterial(request.getMaterial());
        pattern.setRackNumber(request.getRackNumber());

        pattern.setUpdatedBy(SecurityUtils.getCurrentUsername());

        return patternMapper.toResponse(pattern);
    }

    @Override
    public PatternResponse changeStatus(UUID id, PatternStatusUpdateRequest request) {
        Pattern pattern = patternRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Pattern not found"));

        PatternStatus newStatus = request.getStatus();

        // Business Rules
        if (pattern.getStatus() == PatternStatus.IN_USE && newStatus == PatternStatus.SCRAPPED){
            throw new IllegalStateException("Cannot scrap pattern while it is in us");
        }
        pattern.setStatus(newStatus);
        pattern.setUpdatedBy(SecurityUtils.getCurrentUsername());

        return patternMapper.toResponse(pattern);

    }
}
