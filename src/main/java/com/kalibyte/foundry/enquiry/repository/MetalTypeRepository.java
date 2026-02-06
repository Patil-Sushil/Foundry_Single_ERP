package com.kalibyte.foundry.enquiry.repository;

import com.kalibyte.foundry.enquiry.entity.MetalType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetalTypeRepository extends JpaRepository<MetalType, Long> {

}