package com.kalibyte.foundry.enquiry.repository;

import com.kalibyte.foundry.enquiry.entity.MetalCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetalCategoryRepository extends JpaRepository<MetalCategory, Long> {
}