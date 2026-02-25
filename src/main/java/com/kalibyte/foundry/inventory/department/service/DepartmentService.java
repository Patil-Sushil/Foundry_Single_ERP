package com.kalibyte.foundry.inventory.department.service;

import com.kalibyte.foundry.inventory.department.dto.request.DepartmentRequest;
import com.kalibyte.foundry.inventory.department.dto.response.DepartmentResponse;
import com.kalibyte.foundry.inventory.department.entity.Department;
import com.kalibyte.foundry.inventory.department.exception.DuplicateDepartmentException;
import com.kalibyte.foundry.inventory.department.repository.DepartmentRepository;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;

	public DepartmentService(DepartmentRepository departmentRepository, ModelMapper modelMapper) {
		this.departmentRepository = departmentRepository;
		this.modelMapper = modelMapper;
	}

	@Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream()
                .map(department->modelMapper.map(department,DepartmentResponse.class)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return modelMapper.map(department,DepartmentResponse.class);
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest departmentRequest) {
        Department dept=departmentRepository.findByCode(departmentRequest.getCode());
        if(dept!=null){
            throw new DuplicateDepartmentException("Department code already exists");
        }
        Department department = Department.builder()
                .name(departmentRequest.getName())
                .code(departmentRequest.getCode())
                .build();
        return modelMapper.map(departmentRepository.save(department), DepartmentResponse.class);
    }
}
