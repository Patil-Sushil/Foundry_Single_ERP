package com.kalibyte.foundry.expenses.repository;

import com.kalibyte.foundry.expenses.entity.ExpenseHead;
import com.kalibyte.foundry.expenses.entity.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseHeadRepository extends JpaRepository<ExpenseHead, UUID> {
    Optional<ExpenseHead> findByNameIgnoreCase(String name);
    Optional<ExpenseHead> findByNameIgnoreCaseAndCategory(String name, ExpenseCategory category);


}