package com.kalibyte.foundry.inventory.inward.repository;

import com.kalibyte.foundry.inventory.inward.entity.ReceivedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceivedItemRepository extends JpaRepository<ReceivedItem, Long> {
}
