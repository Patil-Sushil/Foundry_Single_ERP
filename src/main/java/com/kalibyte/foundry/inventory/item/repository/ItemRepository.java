package com.kalibyte.foundry.inventory.item.repository;

import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    Page<Item> findByIsActive(Boolean isActive, Pageable pageable);

    Page<Item> findByCategoryAndIsActive(ItemCategory category, Boolean isActive, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.isActive = true AND i.currentStock <= i.reorderLevel")
    List<Item> findByCurrentStockLessThanEqualAndIsActiveTrue();

    @Query("SELECT i FROM Item i WHERE i.isActive = true AND " +
           "(LOWER(i.name) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
           "LOWER(i.code) LIKE LOWER(CONCAT('%',:query,'%')))")
    Page<Item> findByNameOrCode(@Param("query") String query, Pageable pageable);

    java.util.Optional<Item> findByIsScrapTrueAndGrade(String grade);

    Page<Item> findByIsScrap(Boolean isScrap, org.springframework.data.domain.Pageable pageable);
}
