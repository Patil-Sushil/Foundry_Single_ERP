package com.kalibyte.foundry.quotation.repository;

import com.kalibyte.foundry.quotation.entity.QuotationApproval;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationApprovalRepository extends JpaRepository<QuotationApproval, Long> {
}