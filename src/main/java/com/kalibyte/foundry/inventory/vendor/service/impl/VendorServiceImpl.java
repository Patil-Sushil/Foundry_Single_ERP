package com.kalibyte.foundry.inventory.vendor.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.inventory.vendor.dto.request.CreateVendorRequest;
import com.kalibyte.foundry.inventory.vendor.dto.request.UpdateVendorRequest;
import com.kalibyte.foundry.inventory.vendor.dto.response.VendorResponse;
import com.kalibyte.foundry.inventory.vendor.dto.response.VendorSummary;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import com.kalibyte.foundry.inventory.vendor.exception.DuplicateVendorException;
import com.kalibyte.foundry.inventory.vendor.mapper.VendorMapper;
import com.kalibyte.foundry.inventory.vendor.repository.VendorRepository;
import com.kalibyte.foundry.inventory.vendor.service.VendorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    private final VendorMapper vendorMapper;

    @Override
    @Transactional
    @CacheEvict(value = "vendors", allEntries = true)
    public VendorResponse create(CreateVendorRequest request) {
        if (vendorRepository.findByPhone(request.phone()) != null) {
            throw new DuplicateVendorException("Vendor by the phone :" + request.phone() + " is already in database");
        }
        Vendor vendor = vendorMapper.toEntity(request);
        return vendorMapper.toResponse(vendorRepository.save(vendor));
    }

    @Override
    @Transactional
    @CacheEvict(value = "vendors", allEntries = true)
    public VendorResponse update(Long id, UpdateVendorRequest request) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        vendorMapper.updateEntity(request, vendor);

        return vendorMapper.toResponse(vendorRepository.save(vendor));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "vendors", key = "#id")
    public VendorResponse getById(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        return vendorMapper.toResponse(vendor);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VendorResponse> getAll(Boolean isActive, Pageable pageable) {
        Page<Vendor> vendors;
        if (isActive != null) {
            vendors = vendorRepository.findByIsActive(isActive, pageable);
        } else {
            vendors = vendorRepository.findAll(pageable);
        }
        return PageResponse.from(vendors.map(vendorMapper::toResponse));
    }

    @Override
    @Transactional
    @CacheEvict(value = "vendors", allEntries = true)
    public void deactivate(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        vendor.setIsActive(false);
        vendorRepository.save(vendor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorSummary> search(String query) {
        Pageable limit = PageRequest.of(0, 10);
        return vendorRepository.findByNameContainingIgnoreCaseOrPhoneContaining(query, query, limit)
                .stream()
                .map(vendorMapper::toSummary)
                .toList();
    }
}
