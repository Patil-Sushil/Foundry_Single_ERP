package com.kalibyte.foundry.quotation.repository;

import com.kalibyte.foundry.quotation.entity.Quotation;
import com.kalibyte.foundry.quotation.entity.enums.QuotationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, UUID> {

    Optional<Quotation> findTopByQuotationNumberStartingWithOrderByQuotationNumberDesc(String prefix);

    @EntityGraph(attributePaths = {
            "customer",
            "items",
            "enquiry"
    })
    @Query("""
        SELECT q FROM Quotation q
        LEFT JOIN FETCH q.items i
        LEFT JOIN FETCH i.pattern
        LEFT JOIN FETCH i.patternReceipt
        WHERE q.id = :id
        """)
    Optional<Quotation> findByIdWithItems(UUID id);
    Page<Quotation> findByStatus(QuotationStatus status, Pageable pageable);

    boolean existsByEnquiryIdAndStatusNot(UUID enquiryId, QuotationStatus status);

    java.util.List<Quotation> findAllByEnquiryIdAndStatusNot(UUID enquiryId, QuotationStatus status);
}