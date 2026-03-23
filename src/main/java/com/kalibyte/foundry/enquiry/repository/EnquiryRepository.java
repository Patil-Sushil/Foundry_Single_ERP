package com.kalibyte.foundry.enquiry.repository;

import com.kalibyte.foundry.enquiry.entity.Enquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnquiryRepository extends JpaRepository<Enquiry, UUID> {

    @EntityGraph(attributePaths = {"customer", "enquiryItems"})
    Optional<Enquiry> findById(UUID id);

    @EntityGraph(attributePaths = {"customer"})
    Page<Enquiry> findAll(Pageable pageable);

    Optional<Enquiry> findTopByEnquiryNoStartingWithOrderByEnquiryNoDesc(String prefix);


    // Solving N+1
    @EntityGraph(attributePaths = {
            "customer",
            "enquiryItems"
    })
    Page<Enquiry>findByCustomerId(UUID customerId, Pageable pageable);
}
