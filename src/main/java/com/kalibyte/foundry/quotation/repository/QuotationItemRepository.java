package com.kalibyte.foundry.quotation.repository;

import com.kalibyte.foundry.quotation.entity.QuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuotationItemRepository extends JpaRepository<QuotationItem, UUID> {

}