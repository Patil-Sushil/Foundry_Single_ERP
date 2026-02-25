package com.kalibyte.foundry.inventory.vendor.service;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.inventory.vendor.dto.request.CreateVendorRequest;
import com.kalibyte.foundry.inventory.vendor.dto.request.UpdateVendorRequest;
import com.kalibyte.foundry.inventory.vendor.dto.response.VendorResponse;
import com.kalibyte.foundry.inventory.vendor.dto.response.VendorSummary;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import com.kalibyte.foundry.inventory.vendor.exception.DuplicateVendorException;
import com.kalibyte.foundry.inventory.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;

    @Transactional
    public VendorResponse create(CreateVendorRequest request) {
        Vendor vendor1 = vendorRepository.findByPhone(request.phone());
        if(vendor1 != null){
            throw new DuplicateVendorException("Vendor by the phone :"+ request.phone() +" is already in database");
        }
        Vendor vendor = Vendor.builder()
                .name(request.name())
                .phone(request.phone())
                .gstNumber(request.gstNumber())
                .address(request.address())
                .isActive(true)
                .build();
        
        return toResponse(vendorRepository.save(vendor));
    }

    @Transactional
    public VendorResponse update(Long id, UpdateVendorRequest request) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        vendor.setName(request.name());
        vendor.setPhone(request.phone());
        vendor.setGstNumber(request.gstNumber());
        vendor.setAddress(request.address());
        if (request.isActive() != null) {
            vendor.setIsActive(request.isActive());
        }

        return toResponse(vendorRepository.save(vendor));
    }

    @Transactional(readOnly = true)
    public VendorResponse getById(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        return toResponse(vendor);
    }

    @Transactional(readOnly = true)
    public Page<VendorResponse> getAll(Boolean isActive, Pageable pageable) {
        Page<Vendor> vendors;
        if (isActive != null) {
            vendors = vendorRepository.findByIsActive(isActive, pageable);
        } else {
            vendors = vendorRepository.findAll(pageable);
        }
        return vendors.map(this::toResponse);
    }

    @Transactional
    public void deactivate(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        vendor.setIsActive(false);
        vendorRepository.save(vendor);
    }

    @Transactional(readOnly = true)
    public List<VendorSummary> search(String query) {
        Pageable limit = PageRequest.of(0, 10);
        return vendorRepository.findByNameContainingIgnoreCaseOrPhoneContaining(query, query, limit)
                .stream()
                .map(v -> new VendorSummary(v.getId(), v.getName(), v.getPhone()))
                .toList();
    }

    private VendorResponse toResponse(Vendor vendor) {
        return new VendorResponse(
                vendor.getId(),
                vendor.getName(),
                vendor.getPhone(),
                vendor.getGstNumber(),
                vendor.getAddress(),
                vendor.getIsActive(),
                vendor.getCreatedAt(),
                vendor.getUpdatedAt()
        );
    }
}
