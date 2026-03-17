package com.kalibyte.foundry.expenses.service;

import com.kalibyte.foundry.expenses.dto.response.ExpenseHeadResponse;

import java.util.List;

public interface ExpenseHeadService {

    List<ExpenseHeadResponse> getAllExpenseHeads();
}
