package com.kalibyte.foundry.labors.labor.repository;

import com.kalibyte.foundry.labors.labor.entity.Laborer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaborerRepository extends JpaRepository<Laborer, Long> {
    List<Laborer> findByIsActiveTrue();
}
