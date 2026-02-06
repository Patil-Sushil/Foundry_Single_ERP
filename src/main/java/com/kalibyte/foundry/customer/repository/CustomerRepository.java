package com.kalibyte.foundry.customer.repository;

import com.kalibyte.foundry.customer.dto.CustomerResponse;
import com.kalibyte.foundry.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);
    Page<Customer> findAll(Pageable pageable);
    boolean existsByEmail(String email);

    Optional<CustomerResponse> findByPhone(String phone);

}
