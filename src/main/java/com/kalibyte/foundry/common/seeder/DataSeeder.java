package com.kalibyte.foundry.common.seeder;

import com.kalibyte.foundry.auth.entity.Role;
import com.kalibyte.foundry.auth.entity.User;
import com.kalibyte.foundry.auth.repository.RoleRepository;
import com.kalibyte.foundry.users.repository.UserRepository;
import com.kalibyte.foundry.auth.service.AuthService;
import com.kalibyte.foundry.superadmin.dto.FoundryRegistrationRequest;
import com.kalibyte.foundry.users.dto.UserRegistrationRequest;
import com.kalibyte.foundry.tenant.account.entity.TenantEntity;
import com.kalibyte.foundry.tenant.account.service.TenantService;
import com.kalibyte.foundry.customer.dto.CustomerRequest;
import com.kalibyte.foundry.customer.service.CustomerService;
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
    private final CustomerService customerService;

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
                "SUPER_ADMIN", "ADMIN", "PRODUCTION", "SALES", "STORE"
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
            log.info("Tenant {} already exists (owner: {}), ensuring customers are seeded...", name, ownerEmail);
            try {
                User owner = userRepository.findByEmail(ownerEmail).orElseThrow();
                if (owner.getTenantId() == null) {
                    log.warn("Owner {} exists but has no tenantId. Skipping.", ownerEmail);
                    return;
                }
                
                TenantEntity tenant = tenantService.findById(owner.getTenantId());
                
                // Set context to existing tenant to check customers
                CustomUserDetails principal = CustomUserDetails.create(owner, tenant.getCode(), tenant.getSchemaName());
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
                );
                ContextUtil.setTenant(tenant.getSchemaName());
                
                seedCustomers();
            } catch (Exception e) {
                log.error("Failed to check/seed customers for existing tenant: " + name, e);
            } finally {
                SecurityContextHolder.clearContext();
                ContextUtil.clear();
            }
            return;
        }

        log.info("Seeding Tenant: {}", name);
        try {
            // Register Foundry (Creates Tenant + Schema + Admin User)
            FoundryRegistrationRequest req = new FoundryRegistrationRequest();
            req.setFoundryName(name);
            req.setAddress("123 Industrial Area");
            req.setGstNumber("GST" + code.toUpperCase());
            req.setOwnerName("test");
            req.setOwnerEmail(ownerEmail);
            req.setOwnerPassword("Admin@123");
            req.setOwnerPhone("8888888888");
            TenantEntity tenant = authService.registerFoundry(req);
            log.info("Registered tenant: {} with schema: {}", name, tenant.getSchemaName());

            // Mock Security Context as the new Admin to create sub-users
            User owner = userRepository.findByEmail(ownerEmail).orElseThrow();
            
            // Set Security Context
            CustomUserDetails principal = CustomUserDetails.create(owner, tenant.getCode(), tenant.getSchemaName());
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
            );

            // Set Tenant Context for Hibernate
            ContextUtil.setTenant(tenant.getSchemaName());
            log.debug("Context set for schema: {}", tenant.getSchemaName());

            // Create Sub Users
            String actualCode = tenant.getCode();
            createSubUser("production@" + actualCode + ".foundry.com", "PRODUCTION");
            createSubUser("sales@" + actualCode + ".foundry.com", "SALES");
            createSubUser("store@" + actualCode + ".foundry.com", "STORE");

            // Seed Customers
            seedCustomers();
            log.info("Successfully seeded tenant: {}", name);

        } catch (Exception e) {
            log.error("Failed to seed tenant: " + name, e);
        } finally {
            SecurityContextHolder.clearContext();
            ContextUtil.clear();
        }
    }

    private void seedCustomers() {
        log.debug("Checking if customers need seeding...");
        try {
            // This query will now use the correct schema due to ContextUtil.setTenant()
            if (customerService.listCustomers(0, 10, "name").getTotalElements() > 0) {
                log.debug("Customers already exist, skipping seeding.");
                return;
            }
            
            log.info("Seeding customers into tenant schema...");
            createCustomer("Acme Corp", "acme@company.com", "NET30");
            createCustomer("TechFlow Industries", "tech@company.com", "NET60");
            createCustomer("Steel Traders", "steel@company.com", "ADVANCE");
            createCustomer("Metal Suppliers", "metal@company.com", "COD");
            createCustomer("Iron Works", "iron@company.com", "NET30");
            log.info("Finished seeding customers.");
        } catch (Exception e) {
            log.error("Error during customer seeding: {}. This usually means the table does not exist in the tenant schema.", e.getMessage());
        }
    }

    private void createCustomer(String name, String email, String terms) {
        CustomerRequest req = new CustomerRequest();
        req.setName(name);
        req.setEmail(email);
        req.setPaymentTerms(terms);
        req.setCreditLimit(new java.math.BigDecimal("100000.00"));
        customerService.createCustomer(req);
    }

    private void createSubUser(String email, String role) {
        if (!userRepository.existsByEmail(email)) {
            UserRegistrationRequest req = new UserRegistrationRequest();
            req.setEmail(email);
            req.setPassword("User@123");
            req.setPhone("1234567890");
            req.setRole(role);
            authService.createUser(req);
        }
    }
}