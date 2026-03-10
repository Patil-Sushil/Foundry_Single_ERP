package com.kalibyte.foundry.expenses.mapper;


import com.kalibyte.foundry.expenses.dto.response.ExpenseResponse;
import com.kalibyte.foundry.expenses.entity.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(source = "expenseHead.id", target = "expenseHeadId")
    @Mapping(source = "expenseHead.name", target = "expenseHeadName")
    ExpenseResponse toResponse(Expense expense);
}