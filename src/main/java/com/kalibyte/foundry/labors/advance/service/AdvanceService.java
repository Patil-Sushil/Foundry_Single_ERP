package com.kalibyte.foundry.labors.advance.service;

import com.kalibyte.foundry.labors.advance.dto.AdvanceTransactionRequest;
import com.kalibyte.foundry.labors.advance.dto.AdvanceTransactionResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AdvanceService {
    AdvanceTransactionResponse grantAdvance(AdvanceTransactionRequest request);
    AdvanceTransactionResponse deductAdvance(AdvanceTransactionRequest request);
    BigDecimal getOutstandingBalance(Long laborerId);
    List<AdvanceTransactionResponse> getTransactionsByLaborer(Long laborerId);
}
