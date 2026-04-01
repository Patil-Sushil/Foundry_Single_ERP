package com.kalibyte.foundry.expenses.mapper;

import com.kalibyte.foundry.expenses.dto.response.ExpenseHeadResponse;
import com.kalibyte.foundry.expenses.entity.ExpenseHead;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpenseHeadMapper {

    ExpenseHeadResponse toResponse(ExpenseHead head);
}
