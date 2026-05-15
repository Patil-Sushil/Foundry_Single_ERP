package com.kalibyte.foundry.labors.advance.service.impl;

import com.kalibyte.foundry.labors.advance.dto.AdvanceTransactionRequest;
import com.kalibyte.foundry.labors.advance.dto.AdvanceTransactionResponse;
import com.kalibyte.foundry.labors.advance.entity.AdvanceTransaction;
import com.kalibyte.foundry.labors.advance.entity.Enum.TransactionType;
import com.kalibyte.foundry.labors.advance.mapper.AdvanceTransactionMapper;
import com.kalibyte.foundry.labors.advance.repository.AdvanceTransactionRepository;
import com.kalibyte.foundry.labors.advance.service.AdvanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvanceServiceImpl implements AdvanceService {

    private final AdvanceTransactionRepository advanceTransactionRepository;
    private final AdvanceTransactionMapper advanceTransactionMapper;

    @Override
    @Transactional
    public AdvanceTransactionResponse grantAdvance(AdvanceTransactionRequest request) {
        AdvanceTransaction transaction = advanceTransactionMapper.toEntity(request);
        transaction.setTransactionType(TransactionType.GIVEN);
        return advanceTransactionMapper.toResponseDTO(advanceTransactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public AdvanceTransactionResponse deductAdvance(AdvanceTransactionRequest request) {
        AdvanceTransaction transaction = advanceTransactionMapper.toEntity(request);
        transaction.setTransactionType(TransactionType.DEDUCTED);
        return advanceTransactionMapper.toResponseDTO(advanceTransactionRepository.save(transaction));
    }

    @Override
    public BigDecimal getOutstandingBalance(Long laborerId) {
        return advanceTransactionRepository.getOutstandingBalance(laborerId);
    }

    @Override
    public List<AdvanceTransactionResponse> getTransactionsByLaborer(Long laborerId) {
        return advanceTransactionRepository.findByLaborerId(laborerId).stream()
                .map(advanceTransactionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
