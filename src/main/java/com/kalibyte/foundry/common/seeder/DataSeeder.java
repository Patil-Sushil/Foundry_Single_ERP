package com.kalibyte.foundry.common.seeder;

import com.kalibyte.foundry.auth.entity.Role;
import com.kalibyte.foundry.auth.entity.User;
import com.kalibyte.foundry.auth.repository.RoleRepository;
import com.kalibyte.foundry.auth.repository.UserRepository;
import com.kalibyte.foundry.auth.service.AuthService;
import com.kalibyte.foundry.auth.dto.FoundryRegistrationRequest;
import com.kalibyte.foundry.auth.dto.UserRegistrationRequest;
import com.kalibyte.foundry.tenant.account.entity.TenantEntity;
import com.kalibyte.foundry.tenant.account.service.TenantService;
import com.kalibyte.foundry.common.util.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.kalibyte.foundry.auth.security.token.CustomUserDetails;

import java.util.Collections;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedSuperAdmin();
        seedDummyTenants();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            log.info("Seeding Roles...");
            List<String> roles = Arrays.asList(
                "SUPER_ADMIN", "ADMIN", "PRODUCTION", "SALES", "STORE", "INVENTORY"
            );
            
            for (String roleName : roles) {
                roleRepository.save(new Role(null, roleName, roleName + " Role"));
            }
        }
    }

    private void seedSuperAdmin() {
        if (!userRepository.existsByEmail("superadmin@foundry.com")) {
            log.info("Seeding Super Admin...");
            User admin = new User();
            admin.setEmail("superadmin@foundry.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setEnabled(true);
            
            Role adminRole = roleRepository.findByName("SUPER_ADMIN").orElseThrow();
            admin.setRoles(new HashSet<>(Collections.singletonList(adminRole)));
            
            userRepository.save(admin);
        }
    }

    private void seedDummyTenants() {
        seedTenant("foundry_alpha", "Alpha Foundry", "admin@foundry_alpha.foundry.com");
        seedTenant("foundry_beta", "Beta Foundry", "admin@foundry_beta.foundry.com");
    }

    private void seedTenant(String code, String name, String ownerEmail) {
        if (userRepository.existsByEmail(ownerEmail)) {
            return;
        }

        log.info("Seeding Tenant: {}", name);
        try {
            // Register Foundry (Creates Tenant + Schema + Admin User)
            FoundryRegistrationRequest req = new FoundryRegistrationRequest();
            req.setFoundryName(name);
            req.setAddress("123 Industrial Area");
            req.setGstNumber("GST" + code.toUpperCase());
            req.setOwnerEmail(ownerEmail);
            req.setOwnerPassword("Admin@123");
            TenantEntity tenant = authService.registerFoundry(req);

            // Mock Security Context as the new Admin to create sub-users
            User owner = userRepository.findByEmail(ownerEmail).orElseThrow();
            
            CustomUserDetails principal = CustomUserDetails.create(owner, tenant.getCode(), tenant.getSchemaName());
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
            );

            // Create Sub Users
            // Use tenant.getCode() because the actual code has a timestamp appended
            String actualCode = tenant.getCode();
            createSubUser("production@" + actualCode + ".foundry.com", "PRODUCTION");
            createSubUser("sales@" + actualCode + ".foundry.com", "SALES");
            createSubUser("store@" + actualCode + ".foundry.com", "STORE");

            SecurityContextHolder.clearContext();

        } catch (Exception e) {
            log.error("Failed to seed tenant: " + name, e);
        }
    }

    private void createSubUser(String email, String role) {
        if (!userRepository.existsByEmail(email)) {
            UserRegistrationRequest req = new UserRegistrationRequest();
            req.setEmail(email);
            req.setPassword("User@123");
            req.setRole(role);
            authService.createUser(req);
        }
    }
}