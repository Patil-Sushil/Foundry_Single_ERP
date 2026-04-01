package com.kalibyte.foundry.scrap.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapItemRequest {
    private Long id;
    private Long itemId;
    private String itemName;
    private String itemCode;
    private String grade;
    private String scrapType;
    private Integer quantity;
    private BigDecimal weight;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private String defectType;
    private String recyclability;
    private String destination;
    private Long inspectionDefectId;
}
