package com.kalibyte.foundry.inventory.department.service;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.inventory.department.dto.request.DepartmentRequest;
import com.kalibyte.foundry.inventory.department.dto.response.DepartmentResponse;
import com.kalibyte.foundry.inventory.department.entity.Department;
import com.kalibyte.foundry.inventory.department.exception.DuplicateDepartmentException;
import com.kalibyte.foundry.inventory.department.mapper.DepartmentMapper;
import com.kalibyte.foundry.inventory.department.repository.DepartmentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

	public DepartmentService(DepartmentRepository departmentRepository, DepartmentMapper departmentMapper) {
		this.departmentRepository = departmentRepository;
		this.departmentMapper = departmentMapper;
	}

	@Transactional(readOnly = true)
    @Cacheable(value = "departments")
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "departments", key = "#id")
    public DepartmentResponse getById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return departmentMapper.toResponse(department);
    }

    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    public DepartmentResponse createDepartment(DepartmentRequest departmentRequest) {
        Department dept=departmentRepository.findByCode(departmentRequest.getCode());
        if(dept!=null){
            throw new DuplicateDepartmentException("Department code already exists");
        }
        Department department = departmentMapper.toEntity(departmentRequest);
        return departmentMapper.toResponse(departmentRepository.save(department));
    }
}
