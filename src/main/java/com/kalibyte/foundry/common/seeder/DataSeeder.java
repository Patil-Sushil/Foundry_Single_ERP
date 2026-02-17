package com.kalibyte.foundry.common.seeder;

import com.kalibyte.foundry.auth.entity.ENUMS.RoleName;
import com.kalibyte.foundry.auth.entity.Role;
import com.kalibyte.foundry.auth.repository.RoleRepository;
import com.kalibyte.foundry.customer.dto.CustomerRequest;
import com.kalibyte.foundry.customer.service.CustomerService;
import com.kalibyte.foundry.enquiry.entity.MetalCategory;
import com.kalibyte.foundry.enquiry.entity.MetalType;
import com.kalibyte.foundry.enquiry.repository.MetalCategoryRepository;
import com.kalibyte.foundry.enquiry.repository.MetalTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev") // Only runs in dev profile
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final MetalCategoryRepository metalCategoryRepository;
    private final MetalTypeRepository metalTypeRepository;
    private final CustomerService customerService;

    @Override
    public void run(String... args) {
        seedRoles();
        seedMetalMasters();
        seedCustomers();
    }

    /* ---------------- ROLES ---------------- */

    private void seedRoles() {

        log.info("Seeding Roles...");

        for (RoleName roleName : RoleName.values()) {

            roleRepository.findByName(roleName)
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName(roleName);
                        role.setDescription(roleName.name() + " Role");
                        return roleRepository.save(role);
                    });
        }

        log.info("Roles seeded successfully.");
    }

    /* ---------------- METAL MASTER DATA ---------------- */

    private void seedMetalMasters() {

        if (metalCategoryRepository.count() > 0) {
            log.info("Metal master already exists. Skipping.");
            return;
        }

        log.info("Seeding Metal Categories & Types...");

        Map<String, List<String>> data = Map.of(
                "Ferrous", List.of("Cast Iron", "SG Iron", "Mild Steel"),
                "Non-Ferrous", List.of("Aluminium", "Copper", "Brass"),
                "Alloy Steel", List.of("SS 304", "SS 316")
        );

        data.forEach((categoryName, types) -> {

            MetalCategory category = new MetalCategory();
            category.setName(categoryName);
            category.setActive(true);

            metalCategoryRepository.save(category);

            types.forEach(typeName -> {
                MetalType type = new MetalType();
                type.setName(typeName);
                type.setCategory(category);
                type.setActive(true);
                metalTypeRepository.save(type);
            });
        });

        log.info("Metal master seeded.");
    }

    /* ---------------- SAMPLE CUSTOMERS ---------------- */

    private void seedCustomers() {

        if (customerService.listCustomers(0, 1, "name").getTotalElements() > 0) {
            log.info("Customers already exist. Skipping.");
            return;
        }

        log.info("Seeding sample customers...");

        createCustomer("Acme Castings", "acme@company.com");
        createCustomer("Steel Industries", "steel@company.com");
        createCustomer("Metal Traders", "metal@company.com");

        log.info("Customers seeded.");
    }

    private void createCustomer(String name, String email) {

        CustomerRequest req = new CustomerRequest();
        req.setName(name);
        req.setEmail(email);
        req.setPaymentTerms("NET30");
        req.setCreditLimit(new BigDecimal("100000"));

        customerService.createCustomer(req);
    }
}
