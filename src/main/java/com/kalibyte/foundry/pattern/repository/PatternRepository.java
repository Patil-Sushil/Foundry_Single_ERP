package com.kalibyte.foundry.pattern.repository;

import com.kalibyte.foundry.pattern.entity.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PatternRepository extends JpaRepository<Pattern, UUID> {
}