package com.kalibyte.foundry.config;

import com.kalibyte.foundry.infrastructure.tenancy.interceptor.TenantRequiredInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantRequiredInterceptor tenantRequiredInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantRequiredInterceptor)
                .addPathPatterns("/api/**");
    }
}
