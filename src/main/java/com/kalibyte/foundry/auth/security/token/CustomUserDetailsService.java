package com.kalibyte.foundry.auth.security.token;

import com.kalibyte.foundry.auth.entity.User;
import com.kalibyte.foundry.auth.repository.UserRepository;
import com.kalibyte.foundry.tenant.account.entity.TenantEntity;
import com.kalibyte.foundry.tenant.account.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String tenantCode = null;
        String schemaName = "public";

        if (user.getTenantId() != null) {
            TenantEntity tenant = tenantRepository.findById(user.getTenantId())
                    .orElseThrow(() -> new UsernameNotFoundException("Tenant not found for user: " + email));
            tenantCode = tenant.getCode();
            schemaName = tenant.getSchemaName();
        }

        return CustomUserDetails.create(user, tenantCode, schemaName);
    }
}
