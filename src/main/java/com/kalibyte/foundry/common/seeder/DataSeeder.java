package com.kalibyte.foundry.common.seeder;

import com.kalibyte.foundry.auth.entity.ENUMS.RoleName;
import com.kalibyte.foundry.auth.entity.Role;
import com.kalibyte.foundry.auth.repository.RoleRepository;
import com.kalibyte.foundry.customer.dto.CustomerRequest;
import com.kalibyte.foundry.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("seed") // Only runs in dev profile
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CustomerService customerService;

    @Override
    public void run(String... args) {
        log.info("===== STARTING DATA SEEDING =====");

        seedRoles();
        seedCustomers();

        log.info("===== DATA SEEDING COMPLETE =====");
    }

    /* ---------------- ROLES ---------------- */

    private void seedRoles() {

        log.info("Seeding roles...");

        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName(roleName);
                        role.setDescription(roleName.name() + " Role");
                        return roleRepository.save(role);
                    });
        }

        log.info("Roles seeded.");
    }


    /* ---------------- CUSTOMERS ---------------- */

    private void seedCustomers() {

        if (customerService.listCustomers(0, 1, "name").getTotalElements() > 0) {
            log.info("Customers already exist. Skipping.");
            return;
        }

        log.info("Seeding customers...");

        createCustomer("Acme Castings", "acme@company.com","1111111111");
        createCustomer("Steel Industries", "steel@company.com","2222222222");
        createCustomer("Metal Traders", "metal@company.com","3333333333");

        log.info("Customers seeded.");
    }

    private void createCustomer(String name, String email,String phone) {

        CustomerRequest req = new CustomerRequest();
        req.setName(name);
        req.setEmail(email);
        req.setPhone(phone);
        req.setPaymentTerms("NET30");
        req.setCreditLimit(new BigDecimal("100000"));

        customerService.createCustomer(req);
    }
}