package com.kalibyte.foundry.expenses.controller;


import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.expenses.dto.response.ExpenseHeadResponse;
import com.kalibyte.foundry.expenses.service.ExpenseHeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/expense-heads")
@RequiredArgsConstructor
public class ExpenseHeadController {

    private final ExpenseHeadService expenseHeadService;

    @GetMapping
    public ApiResponse<List<ExpenseHeadResponse>> getAllExpenseHeads(){

        return ApiResponse.success(
                expenseHeadService.getAllExpenseHeads()
        );
    }
}