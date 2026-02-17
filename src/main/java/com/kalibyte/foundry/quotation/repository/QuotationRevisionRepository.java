package com.kalibyte.foundry.quotation.repository;

import com.kalibyte.foundry.quotation.entity.QuotationRevision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationRevisionRepository extends JpaRepository<QuotationRevision, Long> {
}