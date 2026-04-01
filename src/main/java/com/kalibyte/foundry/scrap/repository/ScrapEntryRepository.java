package com.kalibyte.foundry.scrap.repository;

import com.kalibyte.foundry.scrap.entity.ScrapEntry;
import com.kalibyte.foundry.scrap.enums.ScrapStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapEntryRepository extends JpaRepository<ScrapEntry, Long> {
    Optional<ScrapEntry> findByScrapNumber(String scrapNumber);
    long countByScrapNumberStartingWith(String prefix);
    List<ScrapEntry> findByStatus(ScrapStatus status);
    List<ScrapEntry> findByHeatId(Long heatId);
    List<ScrapEntry> findByInspectionId(Long inspectionId);
}
