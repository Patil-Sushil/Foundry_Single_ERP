package com.kalibyte.foundry.infrastructure.tenancy.interceptor;

import com.kalibyte.foundry.common.exception.TenantNotFoundException;
import com.kalibyte.foundry.common.util.ContextUtil;
import com.kalibyte.foundry.infrastructure.tenancy.annotation.TenantRequired;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantRequiredInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {

        if (handler instanceof HandlerMethod method) {

            boolean tenantRequired =
                    method.hasMethodAnnotation(TenantRequired.class)
                            || method.getBeanType().isAnnotationPresent(TenantRequired.class);

            if (tenantRequired && ContextUtil.getTenant() == null) {
                throw new TenantNotFoundException("Tenant context is required");
            }
        }

        return true;
    }
}
