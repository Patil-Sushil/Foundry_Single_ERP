package com.kalibyte.foundry.inventory.issue.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DepartmentConsumptionReport(
    Long departmentId,
    LocalDate fromDate,
    LocalDate toDate,
    List<ConsumptionDetail> items,
    BigDecimal grandTotalValue
) {}
