package com.kalibyte.foundry.auth.bootstrap;

import com.kalibyte.foundry.auth.entity.ENUMS.RoleName;
import com.kalibyte.foundry.auth.entity.Role;
import com.kalibyte.foundry.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Runs BEFORE AdminBootstrap (@Order(2))
public class RoleBootstrap implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        try {
            log.info("Checking roles seeding...");
            Arrays.stream(RoleName.values()).forEach(roleName -> {
                try {
                    if (roleRepository.findByName(roleName).isEmpty()) {
                        Role role = new Role();
                        role.setName(roleName);
                        role.setDescription(roleName.name() + " role");
                        roleRepository.save(role);
                        log.info("Created role: {}", roleName);
                    }
                } catch (Exception e) {
                    log.error("Failed to seed role {}: {}", roleName, e.getMessage());
                }
            });
            log.info("Role seeding check complete.");
        } catch (Exception e) {
            log.warn("Could not seed roles. This might be normal if migrations haven't run yet. Error: {}", e.getMessage());
        }
    }
}
