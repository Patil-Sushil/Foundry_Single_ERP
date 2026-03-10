package com.kalibyte.foundry.expenses.repository;

import com.kalibyte.foundry.expenses.entity.Expense;
import com.kalibyte.foundry.expenses.entity.ExpenseHead;
import com.kalibyte.foundry.expenses.entity.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByExpenseDateBetween(LocalDate from, LocalDate to);

    Optional<Expense> findTopByOrderByCreatedAtDesc();

}