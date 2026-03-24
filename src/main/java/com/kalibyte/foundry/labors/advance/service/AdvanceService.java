package com.kalibyte.foundry.labors.advance.service;

import com.kalibyte.foundry.labors.advance.dto.AdvanceTransactionRequestDTO;
import com.kalibyte.foundry.labors.advance.dto.AdvanceTransactionResponseDTO;
import com.kalibyte.foundry.labors.advance.entity.AdvanceTransaction;
import com.kalibyte.foundry.labors.advance.entity.Enum.TransactionType;
import com.kalibyte.foundry.labors.advance.mapper.AdvanceTransactionMapper;
import com.kalibyte.foundry.labors.advance.repository.AdvanceTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdvanceService {

    private final AdvanceTransactionRepository advanceTransactionRepository;
    private final AdvanceTransactionMapper advanceTransactionMapper;

	public AdvanceService(AdvanceTransactionRepository advanceTransactionRepository, AdvanceTransactionMapper advanceTransactionMapper) {
		this.advanceTransactionRepository = advanceTransactionRepository;
		this.advanceTransactionMapper = advanceTransactionMapper;
	}

    @Transactional
    public AdvanceTransactionResponseDTO grantAdvance(AdvanceTransactionRequestDTO request) {
        AdvanceTransaction transaction = advanceTransactionMapper.toEntity(request);
        transaction.setTransactionType(TransactionType.GIVEN);
        return advanceTransactionMapper.toResponseDTO(advanceTransactionRepository.save(transaction));
    }

    @Transactional
    public AdvanceTransactionResponseDTO deductAdvance(AdvanceTransactionRequestDTO request) {
        AdvanceTransaction transaction = advanceTransactionMapper.toEntity(request);
        transaction.setTransactionType(TransactionType.DEDUCTED);
        return advanceTransactionMapper.toResponseDTO(advanceTransactionRepository.save(transaction));
    }

    public BigDecimal getOutstandingBalance(Long laborerId) {
        return advanceTransactionRepository.getOutstandingBalance(laborerId);
    }

    public List<AdvanceTransactionResponseDTO> getTransactionsByLaborer(Long laborerId) {
        return advanceTransactionRepository.findByLaborerId(laborerId).stream()
                .map(advanceTransactionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
