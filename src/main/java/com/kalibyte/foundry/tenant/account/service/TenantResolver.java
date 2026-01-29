package com.kalibyte.foundry.tenant.account.service;

import com.kalibyte.foundry.tenant.account.dto.TenantContext;
import org.springframework.stereotype.Component;

@Component
public class TenantResolver {
    // This component can be used to resolve tenant from various sources if needed.
    // For now, the JwtAuthenticationFilter and TenantAwareFilter handle the extraction.
    // This might be a placeholder for future complex resolution logic.
}
