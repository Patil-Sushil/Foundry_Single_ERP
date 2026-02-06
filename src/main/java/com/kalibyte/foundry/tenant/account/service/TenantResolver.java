package com.kalibyte.foundry.tenant.account.service;

import com.kalibyte.foundry.common.exception.TenantNotFoundException;
import com.kalibyte.foundry.common.util.TenantUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TenantResolver {

    /**
     * Resolves current tenant schema from request context
     */
    public String resolveCurrentTenant() {
        String tenant = TenantUtils.getCurrentTenant();

        if (tenant == null || tenant.isBlank()) {
            log.error("Tenant not resolved from request context");
            throw new TenantNotFoundException("Tenant not resolved from request");
        }

        return tenant;
    }
}
