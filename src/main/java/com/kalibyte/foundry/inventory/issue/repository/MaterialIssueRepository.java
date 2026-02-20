package com.kalibyte.foundry.inventory.issue.repository;

import com.kalibyte.foundry.inventory.issue.entity.MaterialIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialIssueRepository extends JpaRepository<MaterialIssue, Long> {

    @Query("SELECT m FROM MaterialIssue m " +
           "LEFT JOIN FETCH m.department " +
           "LEFT JOIN FETCH m.issuedItems ii " +
           "LEFT JOIN FETCH ii.item " +
           "WHERE m.id = :id")
    Optional<MaterialIssue> findWithItems(@Param("id") Long id);

    @Query("SELECT m FROM MaterialIssue m WHERE " +
           "(:departmentId IS NULL OR m.department.id = :departmentId) AND " +
           "(CAST(:from AS date) IS NULL OR m.issueDate >= :from) AND " +
           "(CAST(:to AS date) IS NULL OR m.issueDate <= :to) " +
           "ORDER BY m.issueDate DESC")
    Page<MaterialIssue> findAllFiltered(@Param("departmentId") Long departmentId,
                                        @Param("from") LocalDate from,
                                        @Param("to") LocalDate to,
                                        Pageable pageable);

    @Query("SELECT m FROM MaterialIssue m " +
           "LEFT JOIN FETCH m.issuedItems ii " +
           "LEFT JOIN FETCH ii.item " +
           "WHERE m.department.id = :deptId " +
           "AND m.issueDate BETWEEN :from AND :to")
    List<MaterialIssue> findByDepartmentAndDateRange(@Param("deptId") Long deptId,
                                                     @Param("from") LocalDate from,
                                                     @Param("to") LocalDate to);

    @Query("SELECT COUNT(m) FROM MaterialIssue m WHERE YEAR(m.issueDate) = :year")
    long countByYear(@Param("year") int year);
}
