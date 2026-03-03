package com.kalibyte.foundry.pattern.repository;

import com.kalibyte.foundry.pattern.entity.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface PatternRepository extends JpaRepository<Pattern, UUID> {
    @Query(value = "SELECT nextval('pattern_number_seq')", nativeQuery = true)
    Long getNextPatternSequence();}