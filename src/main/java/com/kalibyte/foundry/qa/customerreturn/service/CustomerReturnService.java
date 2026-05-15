package com.kalibyte.foundry.qa.customerreturn.service;

import com.kalibyte.foundry.qa.common.enums.*;
import com.kalibyte.foundry.qa.customerreturn.entity.CustomerReturn;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CustomerReturnService {
    List<CustomerReturn> list(LocalDate startDate, LocalDate endDate, UUID customerId, UUID orderId, ReturnStatus status, ReturnDisposition disposition);
    CustomerReturn getById(Long id);
    CustomerReturn receiveReturn(CustomerReturn returnEntry);
    CustomerReturn assessReturn(Long id, QaFinding finding, RootCauseCategory rootCause, String rootCauseDesc, String inspectorName, String remarks);
    CustomerReturn dispositionReturn(Long id, ReturnDisposition disposition, String remarks, String performedBy, BigDecimal creditAmount, UUID replacementOrderId);
    CustomerReturn closeReturn(Long id, String performedBy);
}
