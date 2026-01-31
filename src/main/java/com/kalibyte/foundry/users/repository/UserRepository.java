package com.kalibyte.foundry.users.repository;

import com.kalibyte.foundry.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.tenantId = :tenantId")
    List<User> findAllByTenantId(Long tenantId);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id AND u.tenantId = :tenantId")
    Optional<User> findByIdAndTenantId(Long id, Long tenantId);
}
