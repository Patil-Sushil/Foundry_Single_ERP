package com.kalibyte.foundry.inventory.inward.repository;

import com.kalibyte.foundry.inventory.inward.entity.MaterialInward;
import com.kalibyte.foundry.inventory.inward.entity.enums.InwardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MaterialInwardRepository extends JpaRepository<MaterialInward, Long> {

    @Query("SELECT m FROM MaterialInward m " +
           "LEFT JOIN FETCH m.vendor " +
           "LEFT JOIN FETCH m.purchaseOrder " +
           "LEFT JOIN FETCH m.receivedItems ri " +
           "LEFT JOIN FETCH ri.item " +
           "WHERE m.id = :id")
    Optional<MaterialInward> findWithFullDetails(@Param("id") Long id);

    @Query("SELECT m FROM MaterialInward m WHERE " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(:vendorId IS NULL OR m.vendor.id = :vendorId) AND " +
           "(CAST(:from AS date) IS NULL OR m.inwardDate >= :from) AND " +
           "(CAST(:to AS date) IS NULL OR m.inwardDate <= :to) " +
           "ORDER BY m.inwardDate DESC")
    Page<MaterialInward> findAllFiltered(@Param("status") InwardStatus status,
                                         @Param("vendorId") Long vendorId,
                                         @Param("from") LocalDate from,
                                         @Param("to") LocalDate to,
                                         Pageable pageable);

    @Query("SELECT COUNT(m) FROM MaterialInward m WHERE YEAR(m.inwardDate) = :year")
    long countByYear(@Param("year") int year);

    boolean existsByInwardNumber(String inwardNumber);
}
