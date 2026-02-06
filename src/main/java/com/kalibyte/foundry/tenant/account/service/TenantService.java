package com.kalibyte.foundry.tenant.account.service;

import com.kalibyte.foundry.tenant.account.entity.TenantEntity;

import java.util.UUID;

public interface TenantService {

    TenantEntity createTenant(String name, String address, String gstNumber);

    TenantEntity findById(Long tenantId);
}
