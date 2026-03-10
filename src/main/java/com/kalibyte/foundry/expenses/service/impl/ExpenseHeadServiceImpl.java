package com.kalibyte.foundry.expenses.service.impl;

import com.kalibyte.foundry.expenses.dto.request.ExpenseHeadResponse;
import com.kalibyte.foundry.expenses.mapper.ExpenseHeadMapper;
import com.kalibyte.foundry.expenses.repository.ExpenseHeadRepository;
import com.kalibyte.foundry.expenses.service.ExpenseHeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseHeadServiceImpl implements ExpenseHeadService {

    private final ExpenseHeadRepository expenseHeadRepository;
    private final ExpenseHeadMapper expenseHeadMapper;

    @Override
    public List<ExpenseHeadResponse> getAllExpenseHeads() {

        return expenseHeadRepository.findAll()
                .stream()
                .map(expenseHeadMapper::toResponse)
                .toList();
    }
}
