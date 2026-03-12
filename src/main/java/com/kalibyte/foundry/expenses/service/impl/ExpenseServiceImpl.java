package com.kalibyte.foundry.expenses.service.impl;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.expenses.dto.request.ExpenseCreateRequest;
import com.kalibyte.foundry.expenses.dto.response.ExpenseResponse;
import com.kalibyte.foundry.expenses.entity.Expense;
import com.kalibyte.foundry.expenses.entity.ExpenseHead;
import com.kalibyte.foundry.expenses.entity.enums.ExpenseCategory;
import com.kalibyte.foundry.expenses.mapper.ExpenseMapper;
import com.kalibyte.foundry.expenses.repository.ExpenseHeadRepository;
import com.kalibyte.foundry.expenses.repository.ExpenseRepository;
import com.kalibyte.foundry.expenses.service.ExpenseService;
import com.kalibyte.foundry.expenses.util.ExpenseNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final ExpenseHeadRepository expenseHeadRepository;
    private final ExpenseNumberGenerator expenseNumberGenerator;

    @Override
    @Transactional
    public ExpenseResponse createExpense(ExpenseCreateRequest request) {

        ExpenseHead head;

        if (request.getExpenseHeadId() != null) {
            head = expenseHeadRepository.findById(request.getExpenseHeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Expense head not found"));
        } else if (StringUtils.hasText(request.getExpenseHeadName())) {

            String normalizedName = request.getExpenseHeadName().trim();
            ExpenseCategory category = request.getCategory() != null ? request.getCategory() : ExpenseCategory.OTHER;

            head = expenseHeadRepository
                    .findByNameIgnoreCaseAndCategory(normalizedName, category)
                    .orElseGet(() -> expenseHeadRepository.save(
                            ExpenseHead.builder()
                                    .name(normalizedName)
                                    .category(category)
                                    .description(request.getDescription())
                                    .build()
                    ));

        } else {
            throw new BusinessException("Expense head is required");
        }

        Expense expense = Expense.builder()
                .expenseNumber(expenseNumberGenerator.generate())
                .expenseHead(head)
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .paymentMode(request.getPaymentMode())
                .referenceNumber(request.getReferenceNumber())
                .remarks(request.getRemarks())
                .build();

        Expense saved = expenseRepository.save(expense);
        return expenseMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpense(UUID id) {
        Expense expense = expenseRepository.findByIdWithExpenseHead(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        return expenseMapper.toResponse(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findAllWithExpenseHead()
                .stream()
                .map(expenseMapper::toResponse)
                .toList();
    }
}