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
    public ExpenseResponse createExpense(ExpenseCreateRequest request) {

        ExpenseHead head;

        /*
         * CASE 1: Expense head selected from dropdown
         */
        if (request.getExpenseHeadId() != null) {

            head = expenseHeadRepository.findById(request.getExpenseHeadId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Expense head not found"));
        }

        /*
         * CASE 2: Manual expense head entered
         */
        else if (StringUtils.hasText(request.getExpenseHeadName())) {

            String normalizedName = request.getExpenseHeadName().trim();

            ExpenseCategory category =
                    request.getCategory() != null
                            ? request.getCategory()
                            : ExpenseCategory.OTHER;

            head = expenseHeadRepository
                    .findByNameIgnoreCaseAndCategory(normalizedName, category)
                    .orElseGet(() -> {

                        ExpenseHead newHead = ExpenseHead.builder()
                                .name(normalizedName)
                                .category(category)
                                .description(request.getDescription())
                                .build();

                        return expenseHeadRepository.save(newHead);
                    });
        }

        /*
         * CASE 3: No head provided
         */
        else {
            throw new BusinessException("Expense head is required");
        }

        /*
         * Create Expense
         */
        Expense expense = Expense.builder()
                .expenseNumber(expenseNumberGenerator.generate())
                .expenseHead(head)
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .paymentMode(request.getPaymentMode())
                .referenceNumber(request.getReferenceNumber())
                .remarks(request.getRemarks())
                .build();

        expenseRepository.save(expense);

        return expenseMapper.toResponse(expense);
    }

    @Override
    public ExpenseResponse getExpense(UUID id) {

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));

        return expenseMapper.toResponse(expense);
    }

    @Override
    public List<ExpenseResponse> getAllExpenses() {

        return expenseRepository.findAll()
                .stream()
                .map(expenseMapper::toResponse)
                .toList();
    }
}