package com.kalibyte.foundry.common.castingprocess.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.castingprocess.dto.CastingProcessRequest;
import com.kalibyte.foundry.common.castingprocess.dto.CastingProcessResponse;
import com.kalibyte.foundry.common.castingprocess.entity.CastingProcessMaster;
import com.kalibyte.foundry.common.castingprocess.mapper.CastingProcessMapper;
import com.kalibyte.foundry.common.castingprocess.repository.CastingProcessRepository;
import com.kalibyte.foundry.common.castingprocess.service.CastingProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CastingProcessServiceImpl implements CastingProcessService {

    private final CastingProcessRepository repository;
    private final CastingProcessMapper mapper;

    @Override
    @Transactional
    @CacheEvict(value = "castingProcesses", allEntries = true)
    public CastingProcessResponse create(CastingProcessRequest request) {
        validateUnique(request.getName(), request.getCode(), null);
        
        CastingProcessMaster entity = mapper.toEntity(request);
        entity.setCode(request.getCode().trim().toUpperCase());
        entity.setName(request.getName().trim());
        
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    @CacheEvict(value = "castingProcesses", allEntries = true)
    public CastingProcessResponse update(UUID id, CastingProcessRequest request) {
        CastingProcessMaster entity = getEntity(id);
        validateUnique(request.getName(), request.getCode(), id);
        
        mapper.update(entity, request);
        entity.setCode(request.getCode().trim().toUpperCase());
        entity.setName(request.getName().trim());
        
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "castingProcesses", key = "#id")
    public CastingProcessResponse get(UUID id) {
        return mapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "castingProcesses")
    public List<CastingProcessResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    list.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                    return list;
                }));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "castingProcesses")
    public List<CastingProcessResponse> getAllActive() {
        return repository.findAllByActiveTrueOrderByNameAsc().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "castingProcesses", allEntries = true)
    public void delete(UUID id) {
        CastingProcessMaster entity = getEntity(id);
        entity.setActive(false);
        repository.save(entity);
    }

    @Override
    public CastingProcessMaster getEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Casting process not found with ID: " + id));
    }

    private void validateUnique(String name, String code, UUID excludeId) {
        repository.findByName(name.trim()).ifPresent(e -> {
            if (excludeId == null || !e.getId().equals(excludeId)) {
                throw new RuntimeException("Casting process with name '" + name + "' already exists");
            }
        });
        repository.findByCode(code.trim().toUpperCase()).ifPresent(e -> {
            if (excludeId == null || !e.getId().equals(excludeId)) {
                throw new RuntimeException("Casting process with code '" + code + "' already exists");
            }
        });
    }
}
