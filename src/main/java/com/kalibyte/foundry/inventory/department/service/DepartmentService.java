package com.kalibyte.foundry.inventory.department.service;

import com.kalibyte.foundry.inventory.department.dto.request.DepartmentRequest;
import com.kalibyte.foundry.inventory.department.dto.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {
    List<DepartmentResponse> getAll();
    DepartmentResponse getById(Long id);
    DepartmentResponse createDepartment(DepartmentRequest departmentRequest);
}
