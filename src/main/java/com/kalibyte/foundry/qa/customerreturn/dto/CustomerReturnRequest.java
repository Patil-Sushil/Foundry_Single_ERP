package com.kalibyte.foundry.qa.customerreturn.dto;

import com.kalibyte.foundry.qa.common.enums.ComplaintCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CustomerReturnRequest {
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    @NotNull(message = "Order ID is required")
    private UUID orderId;
    
    @NotNull(message = "Order item ID is required")
    private UUID orderItemId;
    
    private UUID productionEntryId;
    private Long heatOrderItemId;
    
    @NotNull(message = "Return date is required")
    private LocalDate returnDate;
    
    @NotNull(message = "Returned quantity is required")
    @Min(value = 1, message = "Returned quantity must be at least 1")
    private Integer returnedQuantity;
    
    @NotNull(message = "Complaint category is required")
    private ComplaintCategory complaintCategory;
    
    @NotBlank(message = "Complaint description is required")
    private String complaintDescription;
    
    private String customerReferenceNo;
}
