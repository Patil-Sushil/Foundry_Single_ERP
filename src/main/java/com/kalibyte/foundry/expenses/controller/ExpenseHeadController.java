package com.kalibyte.foundry.expenses.controller;


import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.expenses.dto.response.ExpenseHeadResponse;
import com.kalibyte.foundry.expenses.service.ExpenseHeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
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