package com.kalibyte.foundry.billing.dto.request;

import com.kalibyte.foundry.billing.ENUM.BillStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequest {

    private UUID orderId;

    private String vehicleNumber;

    private LocalDate invoiceDate;

    private LocalDate dueDate;

    private BillStatus billStatus;
}
