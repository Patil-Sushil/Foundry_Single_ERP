package com.kalibyte.foundry.common.seeder;

import com.kalibyte.foundry.auth.entity.Role;
import com.kalibyte.foundry.auth.entity.User;
import com.kalibyte.foundry.auth.repository.RoleRepository;
import com.kalibyte.foundry.auth.security.token.CustomUserDetails;
import com.kalibyte.foundry.customer.dto.CustomerRequest;
import com.kalibyte.foundry.customer.service.CustomerService;
import com.kalibyte.foundry.enquiry.entity.MetalCategory;
import com.kalibyte.foundry.enquiry.entity.MetalType;
import com.kalibyte.foundry.enquiry.repository.MetalCategoryRepository;
import com.kalibyte.foundry.enquiry.repository.MetalTypeRepository;
import com.kalibyte.foundry.superadmin.dto.FoundryRegistrationRequest;
import com.kalibyte.foundry.superadmin.service.impl.SuperAdminServiceImpl;
import com.kalibyte.foundry.tenant.account.entity.TenantEntity;
import com.kalibyte.foundry.tenant.account.service.TenantService;
import com.kalibyte.foundry.users.dto.UserRegistrationRequest;
import com.kalibyte.foundry.users.repository.UserRepository;
import com.kalibyte.foundry.users.service.impl.UserServiceImpl;
import com.kalibyte.foundry.common.util.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserServiceImpl userService;
    private final SuperAdminServiceImpl authService;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;
    private final CustomerService customerService;
    private final MetalCategoryRepository metalCategoryRepository;
    private final MetalTypeRepository metalTypeRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        seedSuperAdmin();
        seedDummyTenants();
    }

    /* ---------------- ROLES ---------------- */

    private void seedRoles() {
        if (roleRepository.count() > 0) return;

        log.info("Seeding Roles...");
        List<String> roles = List.of("SUPER_ADMIN", "ADMIN", "PRODUCTION", "SALES", "STORE");
        roles.forEach(r -> roleRepository.save(new Role(null, r, r + " Role")));
    }

    /* ---------------- SUPER ADMIN ---------------- */

    private void seedSuperAdmin() {
        if (userRepository.existsByEmail("superadmin@foundry.com")) return;

        log.info("Seeding Super Admin...");
        User admin = new User();
        admin.setEmail("superadmin@foundry.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setEnabled(true);
        admin.setPhone("9000000000");

        Role role = roleRepository.findByName("SUPER_ADMIN").orElseThrow();
        admin.setRoles(Set.of(role));

        userRepository.save(admin);
    }

    /* ---------------- TENANTS ---------------- */

    private void seedDummyTenants() {
        seedTenant("foundry_alpha", "Alpha Foundry", "admin@foundry_alpha.foundry.com");
        seedTenant("foundry_beta", "Beta Foundry", "admin@foundry_beta.foundry.com");
    }

    private void seedTenant(String code, String name, String ownerEmail) {

        if (userRepository.existsByEmail(ownerEmail)) {
            log.info("Tenant {} already exists, ensuring master data...", name);
            User owner = userRepository.findByEmail(ownerEmail).orElseThrow();
            TenantEntity tenant = tenantService.findById(owner.getTenantId());
            setTenantContext(owner, tenant);
            seedMetalMasters();
            seedCustomers();
            clearContext();
            return;
        }

        try {
            log.info("Seeding Tenant: {}", name);

            FoundryRegistrationRequest req = new FoundryRegistrationRequest();
            req.setFoundryName(name);
            req.setAddress("123 Industrial Area");
            req.setGstNumber("GST" + code.toUpperCase());
            req.setOwnerName("Admin");
            req.setOwnerEmail(ownerEmail);
            req.setOwnerPassword("Admin@123");
            req.setOwnerPhone(generatePhone());

            TenantEntity tenant = authService.registerFoundry(req);

            User owner = userRepository.findByEmail(ownerEmail).orElseThrow();
            setTenantContext(owner, tenant);

            seedMetalMasters();
            seedCustomers();
            seedSubUsers(tenant);

            log.info("Tenant [{}] seeded successfully", name);

        } catch (Exception e) {
            log.error("Failed to seed tenant: {}", name, e);
        } finally {
            clearContext();
        }
    }

    /* ---------------- METAL MASTER DATA ---------------- */

    private void seedMetalMasters() {
        if (metalCategoryRepository.count() > 0) {
            log.debug("Metal master data already exists, skipping.");
            return;
        }

        log.info("Seeding Metal Categories & Types...");

        Map<String, List<String>> data = Map.of(
                "Ferrous", List.of("Iron", "Steel", "Cast Iron"),
                "Non-Ferrous", List.of("Aluminium", "Copper", "Brass"),
                "Precious", List.of("Gold", "Silver")
        );

        data.forEach((catName, types) -> {
            MetalCategory category = new MetalCategory();
            category.setName(catName);
            category.setActive(true);
            metalCategoryRepository.save(category);

            types.forEach(t -> {
                MetalType type = new MetalType();
                type.setName(t);
                type.setCategory(category);
                metalTypeRepository.save(type);
            });
        });

        log.info("Metal master data seeded.");
    }

    /* ---------------- CUSTOMERS ---------------- */

    private void seedCustomers() {
        if (customerService.listCustomers(0, 1, "name").getTotalElements() > 0) return;

        log.info("Seeding customers...");
        createCustomer("Acme Corp", "acme@company.com");
        createCustomer("Steel Traders", "steel@company.com");
        createCustomer("Metal Suppliers", "metal@company.com");
    }

    private void createCustomer(String name, String email) {
        CustomerRequest req = new CustomerRequest();
        req.setName(name);
        req.setEmail(email);
        req.setPaymentTerms("NET30");
        req.setCreditLimit(new BigDecimal("100000"));
        customerService.createCustomer(req);
    }

    /* ---------------- SUB USERS ---------------- */

    private void seedSubUsers(TenantEntity tenant) {
        createSubUser("production@" + tenant.getCode() + ".foundry.com", "PRODUCTION");
        createSubUser("sales@" + tenant.getCode() + ".foundry.com", "SALES");
        createSubUser("store@" + tenant.getCode() + ".foundry.com", "STORE");
    }

    private void createSubUser(String email, String role) {
        if (userRepository.existsByEmail(email)) return;

        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setEmail(email);
        req.setPassword("User@123");
        req.setPhone(generatePhone());
        req.setRole(role);
        userService.createUser(req);
    }

    /* ---------------- CONTEXT HELPERS ---------------- */

    private void setTenantContext(User user, TenantEntity tenant) {
        CustomUserDetails principal =
                CustomUserDetails.create(user, tenant.getCode(), tenant.getSchemaName());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()
                )
        );

        ContextUtil.setTenant(tenant.getSchemaName());
    }

    private void clearContext() {
        SecurityContextHolder.clearContext();
        ContextUtil.clear();
    }

    private String generatePhone() {
        return "9" + (System.nanoTime() % 1_000_000_000L);
    }
}
