package com.kalibyte.foundry.inventory.purchaseorder.repository;

import com.kalibyte.foundry.inventory.purchaseorder.entity.ItemVendorRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemVendorRateRepository extends JpaRepository<ItemVendorRate, Long> {
    Optional<ItemVendorRate> findByItemIdAndVendorId(Long itemId, Long vendorId);
}
