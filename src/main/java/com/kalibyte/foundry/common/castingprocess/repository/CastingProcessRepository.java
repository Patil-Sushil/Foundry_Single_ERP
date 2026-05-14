package com.kalibyte.foundry.common.castingprocess.repository;

import com.kalibyte.foundry.common.castingprocess.entity.CastingProcessMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CastingProcessRepository extends JpaRepository<CastingProcessMaster, UUID> {

    Optional<CastingProcessMaster> findByCode(String code);

    Optional<CastingProcessMaster> findByName(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCase(String code);

    List<CastingProcessMaster> findAllByActiveTrueOrderByNameAsc();
}
