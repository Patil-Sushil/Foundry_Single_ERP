package com.kalibyte.foundry.superadmin.service.impl;

import com.kalibyte.foundry.auth.entity.Role;
import com.kalibyte.foundry.auth.entity.User;
import com.kalibyte.foundry.auth.repository.RoleRepository;
import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.superadmin.dto.FoundryRegistrationRequest;
import com.kalibyte.foundry.tenant.account.entity.TenantEntity;
import com.kalibyte.foundry.tenant.account.service.TenantService;
import com.kalibyte.foundry.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
@Service
public class SuperAdminServiceImpl {

	private final UserRepository userRepository;
	private final TenantService tenantService;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public SuperAdminServiceImpl(UserRepository userRepository, TenantService tenantService, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.tenantService = tenantService;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public TenantEntity registerFoundry(FoundryRegistrationRequest request) {
		if (userRepository.existsByEmail(request.getOwnerEmail())) {
			throw new BusinessException("Email already in use.");
		}
		// 1-3. Create Tenant & Schema (Delegated to TenantService)
		TenantEntity tenant = tenantService.createTenant(
				request.getFoundryName(),
				request.getAddress(),
				request.getGstNumber()
		);


		// 4. Create Owner User
		User user = new User();
		user.setEmail(request.getOwnerEmail());
		user.setName(request.getOwnerName());
		user.setPhone(request.getOwnerPhone());
		user.setPassword(passwordEncoder.encode(request.getOwnerPassword()));
		user.setTenantId(tenant.getId());
		user.setEnabled(true);

		// 5. Assign ADMIN role
		Role ownerRole = roleRepository.findByName("ADMIN")
				.orElseThrow(() -> new BusinessException("Role ADMIN not found."));
		user.setRoles(new HashSet<>(Collections.singletonList(ownerRole)));

		userRepository.save(user);

		return tenant;
	}
}
