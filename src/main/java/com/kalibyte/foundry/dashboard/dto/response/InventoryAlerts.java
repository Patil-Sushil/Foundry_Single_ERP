package com.kalibyte.foundry.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryAlerts {
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private String periodLabel;

    private List<LowStockItem> lowStockItems;
    private BigDecimal periodProcurementValue;
    private BigDecimal scrapGenerated;
    private BigDecimal scrapRemelted;
    private BigDecimal scrapNetValue;
    private List<VendorSummary> top5Vendors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockItem {
        private Long itemId;
        private String itemName;
        private BigDecimal currentStock;
        private BigDecimal reorderLevel;
        private String unit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorSummary {
        private Long vendorId;
        private String vendorName;
        private BigDecimal mtdProcurementValue;
    }
}
