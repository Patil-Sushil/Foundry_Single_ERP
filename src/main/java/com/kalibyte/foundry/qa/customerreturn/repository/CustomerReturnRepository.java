package com.kalibyte.foundry.qa.customerreturn.repository;

import com.kalibyte.foundry.qa.customerreturn.entity.CustomerReturn;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerReturnRepository extends JpaRepository<CustomerReturn, Long>, JpaSpecificationExecutor<CustomerReturn> {
    @EntityGraph(attributePaths = {"customer", "order", "orderItem", "inspection"})
    Optional<CustomerReturn> findWithDetailsById(Long id);

    @Override
    @EntityGraph(attributePaths = {"customer", "order", "orderItem", "inspection"})
    List<CustomerReturn> findAll(Specification<CustomerReturn> spec);

    Optional<CustomerReturn> findByReturnNumber(String returnNumber);
    long countByReturnNumberStartingWith(String prefix);
    List<CustomerReturn> findByCustomerId(UUID customerId);
}
