package com.kalibyte.foundry.inventory.department.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.inventory.department.dto.request.DepartmentRequest;
import com.kalibyte.foundry.inventory.department.dto.response.DepartmentResponse;
import com.kalibyte.foundry.inventory.department.service.DepartmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@Tag(name = "Departments", description = "Department Management APIs")
public class DepartmentController {

    private final DepartmentService departmentService;

	public DepartmentController(DepartmentService departmentService) {
		this.departmentService = departmentService;
	}

	@GetMapping
    public ApiResponse<List<DepartmentResponse>> getAll() {
        return new ApiResponse<>(true, "Departments retrieved successfully", departmentService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<DepartmentResponse> getById(@PathVariable Long id) {
        return new ApiResponse<>(true, "Department retrieved successfully", departmentService.getById(id));
    }

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<DepartmentResponse> create(@RequestBody DepartmentRequest departmentRequest) {
		return new ApiResponse<>(true,"Department Created Successfully",departmentService.createDepartment(departmentRequest));
	}
}
