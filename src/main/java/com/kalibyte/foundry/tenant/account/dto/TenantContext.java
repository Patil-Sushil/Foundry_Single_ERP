package com.kalibyte.foundry.tenant.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantContext {
    private Long tenantId;
    private String tenantCode;
    private String schemaName;
}
