package com.kalibyte.foundry.expenses.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.expenses.dto.request.ExpenseCreateRequest;
import com.kalibyte.foundry.expenses.dto.response.ExpenseResponse;
import com.kalibyte.foundry.expenses.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // Endpoint to create a new expense
    @PostMapping
    public ApiResponse<ExpenseResponse> createExpense( @RequestBody ExpenseCreateRequest request){
        // Placeholder for creating an expense
        return ApiResponse.success(expenseService.createExpense(request));
    }

    // Endpoint to get an expense by ID
    @GetMapping("/{id}")
    public ApiResponse<ExpenseResponse> getExpense(
            @PathVariable UUID id){

        return ApiResponse.success(
                expenseService.getExpense(id)
        );
    }

    // Endpoint to get all expenses
    @GetMapping
    public ApiResponse<List<ExpenseResponse>> getAllExpenses(){

        return ApiResponse.success(
                expenseService.getAllExpenses()
        );
    }
}
