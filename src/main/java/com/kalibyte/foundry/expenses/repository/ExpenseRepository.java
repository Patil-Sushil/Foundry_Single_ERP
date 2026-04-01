package com.kalibyte.foundry.expenses.repository;

import com.kalibyte.foundry.expenses.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    /**
     * Returns expense totals grouped by expense head.

     * Used for Expense by Head report.
     */
    @Query("""
        SELECT
        e.expenseHead.id,
        e.expenseHead.name,
        SUM(e.amount),
        COUNT(e.id)
        FROM Expense e
        WHERE e.expenseDate BETWEEN :from AND :to
        GROUP BY
        e.expenseHead.id,
        e.expenseHead.name
        ORDER BY SUM(e.amount) DESC
""")
    List<Object[]> getExpenseByHead(LocalDate from, LocalDate to);

    /**
     * Returns expense totals grouped by category.
     */
    @Query("""
        SELECT
        e.expenseHead.category,
        SUM(e.amount),
        COUNT(e.id)
        FROM Expense e
        WHERE e.expenseDate BETWEEN :from AND :to
        GROUP BY e.expenseHead.category
        ORDER BY SUM(e.amount) DESC
        """)
    List<Object[]> getExpenseByCategory(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    /**
     * Returns total operating expenses for the period.
     */
    @Query("""
        SELECT COALESCE(SUM(e.amount),0)
        FROM Expense e
        WHERE e.expenseDate BETWEEN :from AND :to
        """)
    BigDecimal getTotalExpenses(LocalDate from, LocalDate to);

    /**
     * Returns expense totals grouped by Expense Head.
     * Result: [headName, category, totalAmount]
     */
    @Query("""
        SELECT h.name, h.category, SUM(e.amount)
        FROM Expense e
        JOIN e.expenseHead h
        WHERE e.expenseDate BETWEEN :from AND :to
        GROUP BY h.name,h.category
        ORDER BY SUM(e.amount) DESC
        """)
    List<Object[]> getExpenseBreakdown(LocalDate from, LocalDate to);
}