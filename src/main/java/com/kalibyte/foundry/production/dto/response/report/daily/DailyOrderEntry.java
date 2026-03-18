package com.kalibyte.foundry.production.dto.response.report.daily;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyOrderEntry {

    private String orderNumber;
    private String customerName;

    private Integer produced;
    private Integer dispatched;
}
