package com.kalibyte.foundry.users.service.impl;

import com.kalibyte.foundry.users.dto.UserDTO;
import com.kalibyte.foundry.auth.entity.User;
import com.kalibyte.foundry.auth.entity.Role;
import com.kalibyte.foundry.auth.repository.RoleRepository;
import com.kalibyte.foundry.users.dto.UserRegistrationRequest;
import com.kalibyte.foundry.users.repository.UserRepository;
import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.users.service.UserService;
import com.kalibyte.foundry.auth.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

	public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
		this.passwordEncoder = passwordEncoder;
		this.roleRepository = roleRepository;
	}

	public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    @Override
    public List<UserDTO> getAllUsers(Long tenantId) {
        List<User> users = userRepository.findAllByTenantId(tenantId);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id, Long tenantId) {
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = modelMapper.map(user, UserDTO.class);
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        return dto;
    }

    @Transactional
    public UserDTO updateUserById(Long id , Long tenantId, UserRegistrationRequest userRegistrationRequest){
        User user = userRepository.findByIdAndTenantId(id, tenantId)
		        .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        modelMapper.map(user,UserDTO.class);
        user = userRepository.save(user);
        return modelMapper.map(user,UserDTO.class);
    }

    @Override
    @Transactional
    public UserDTO patchUser(Long id, Long tenantId, Map<String, Object> updates) {
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        updates.forEach((key, value) -> {
            // Prevent updating restricted fields
            if (Set.of("id", "tenantId", "password", "email", "roles", "createdAt").contains(key)) {
                return;
            }
            Field field = ReflectionUtils.findField(User.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, user, value);
            }
        });

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public void createUser(UserRegistrationRequest request) {
        // Get current admin user to identify tenant
        String email = SecurityUtils.getCurrentUserEmail();
        User adminUser = getByEmail(email);
        Long tenantId = adminUser.getTenantId();

        if (tenantId == null) {
            throw new BusinessException("Current user is not associated with any tenant.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTenantId(tenantId);
        user.setEnabled(true);

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new BusinessException("Role " + request.getRole() + " not found."));
        user.setRoles(new HashSet<>(Collections.singletonList(role)));

        userRepository.save(user);
    }

}