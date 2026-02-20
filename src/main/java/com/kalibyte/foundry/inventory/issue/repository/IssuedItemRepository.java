package com.kalibyte.foundry.inventory.issue.repository;

import com.kalibyte.foundry.inventory.issue.entity.IssuedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssuedItemRepository extends JpaRepository<IssuedItem, Long> {
}
