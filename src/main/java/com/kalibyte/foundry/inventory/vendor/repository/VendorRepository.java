package com.kalibyte.foundry.inventory.vendor.repository;

import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Page<Vendor> findByIsActive(Boolean isActive, Pageable pageable);

    Page<Vendor> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone, Pageable pageable);

	Vendor findByPhone(String phone);

    Optional<Vendor> findByName(String name);
}
