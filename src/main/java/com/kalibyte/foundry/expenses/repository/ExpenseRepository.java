package com.kalibyte.foundry.expenses.repository;

import com.kalibyte.foundry.expenses.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    // Existing
    List<Expense> findByExpenseDateBetween(LocalDate from, LocalDate to);
    Optional<Expense> findTopByOrderByCreatedAtDesc();

    // NEW: load ExpenseHead together (prevents LazyInitializationException + avoids N+1)
    @Query("""
           select e
           from Expense e
           join fetch e.expenseHead
           order by e.createdAt desc
           """)
    List<Expense> findAllWithExpenseHead();

    @Query("""
           select e
           from Expense e
           join fetch e.expenseHead
           where e.id = :id
           """)
    Optional<Expense> findByIdWithExpenseHead(@Param("id") UUID id);

    @Query("""
           select e
           from Expense e
           join fetch e.expenseHead
           where e.expenseDate between :from and :to
           order by e.expenseDate desc
           """)
    List<Expense> findByExpenseDateBetweenWithExpenseHead(@Param("from") LocalDate from,
                                                          @Param("to") LocalDate to);

    /**
     * Returns total expenses paid per day.
     */
    @Query("""
        SELECT
        e.expenseDate,
        SUM(e.amount)
        FROM Expense e
        WHERE e.expenseDate BETWEEN :from AND :to
        GROUP BY e.expenseDate
        ORDER BY e.expenseDate
        """)
    List<Object[]> getDailyCashOutflow(LocalDate from, LocalDate to);
}