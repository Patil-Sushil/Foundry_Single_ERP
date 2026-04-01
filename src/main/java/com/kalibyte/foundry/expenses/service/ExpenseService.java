package com.kalibyte.foundry.expenses.service;

import com.kalibyte.foundry.expenses.dto.request.ExpenseCreateRequest;
import com.kalibyte.foundry.expenses.dto.response.ExpenseResponse;

import java.util.List;
import java.util.UUID;

public interface ExpenseService {

    ExpenseResponse createExpense(ExpenseCreateRequest request);

    ExpenseResponse getExpense(UUID id);

    List<ExpenseResponse> getAllExpenses();
}