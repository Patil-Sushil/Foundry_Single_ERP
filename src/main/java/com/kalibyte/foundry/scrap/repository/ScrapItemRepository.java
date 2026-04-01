package com.kalibyte.foundry.scrap.repository;

import com.kalibyte.foundry.scrap.entity.ScrapItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapItemRepository extends JpaRepository<ScrapItem, Long> {
    List<ScrapItem> findByScrapEntryId(Long scrapEntryId);
}
