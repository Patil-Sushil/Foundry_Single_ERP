package com.kalibyte.foundry.furnace.furnace_report.repository;

import com.kalibyte.foundry.furnace.furnace_report.entity.Furnace;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FurnaceRepository extends JpaRepository<Furnace, Long> {

    @EntityGraph(attributePaths = {"heats"})
    @Query("SELECT f FROM Furnace f")
    List<Furnace> findAllWithHeats();

    @EntityGraph(attributePaths = {"heats"})
    Optional<Furnace> findById(Long id);

    @Query("SELECT COUNT(f) FROM Furnace f WHERE YEAR(f.date) = :year")
    long countByYear(@Param("year") int year);

    boolean existsByFurnaceRefNo(String furnaceRefNo);

    Optional<Furnace> findByFurnaceRefNo(String furnaceRefNo);
}
