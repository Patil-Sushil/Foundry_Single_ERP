package com.kalibyte.foundry.enquiry.repository;

import com.kalibyte.foundry.enquiry.entity.Enquiry;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.Optional;

public interface EnquiryRepository extends JpaRepository<Enquiry, UUID> {

    Optional<Enquiry> findByIdAndTenantId(java.util.UUID id, Long tenantId);

    Page<Enquiry> findAllByTenantId(Long tenantId, Pageable pageable);

    @Query("""
        SELECT COUNT(e) 
        FROM Enquiry e 
        WHERE e.tenantId = :tenantId 
          AND extract(year from e.enquiryDate) = :year
    """)
    long countForYear(@Param("tenantId") Long tenantId,
                      @Param("year") int year);


    @Component
    public class EnquiryNumberGenerator {

        public String generate(Long tenantId, long countForYear) {
            int year = LocalDate.now().getYear();
            return String.format("ENQ-%d-%05d", year, countForYear + 1);
        }
    }
}