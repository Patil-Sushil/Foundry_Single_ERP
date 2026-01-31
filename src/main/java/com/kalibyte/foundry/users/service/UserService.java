package com.kalibyte.foundry.users.service;

import com.kalibyte.foundry.users.dto.UserDTO;
import com.kalibyte.foundry.users.dto.UserRegistrationRequest;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Map;

public interface UserService {
	List<UserDTO> getAllUsers(Long tenantId);
	UserDTO getUserById(Long id,Long tenantId);
	UserDTO updateUserById(Long id, Long tenantId, UserRegistrationRequest userRegistrationRequest);
    UserDTO patchUser(Long id, Long tenantId, Map<String, Object> updates);
}
