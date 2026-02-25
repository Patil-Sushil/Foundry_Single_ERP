package com.kalibyte.foundry.inventory.department.repository;

import com.kalibyte.foundry.inventory.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
	Department findByCode(String code);
	java.util.Optional<Department> findByName(String name);
}
