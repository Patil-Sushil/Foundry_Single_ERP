package com.kalibyte.foundry.inventory.department.service;

import com.kalibyte.foundry.inventory.department.dto.response.DepartmentResponse;
import com.kalibyte.foundry.inventory.department.entity.Department;
import com.kalibyte.foundry.inventory.department.repository.DepartmentRepository;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return toResponse(department);
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode()
        );
    }
}
