package com.kalibyte.foundry.pattern.repository;

import com.kalibyte.foundry.pattern.entity.PatternReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PatternReceiptRepository extends JpaRepository<PatternReceipt, UUID> {
}