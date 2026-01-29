package com.kalibyte.foundry.tenant.account.repository;

import com.kalibyte.foundry.tenant.account.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<TenantEntity, Long> {
    Optional<TenantEntity> findByCode(String code);
}