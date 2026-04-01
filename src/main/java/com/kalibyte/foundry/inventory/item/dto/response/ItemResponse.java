package com.kalibyte.foundry.inventory.item.dto.response;

import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemSubCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemUnit;
import com.kalibyte.foundry.inventory.item.entity.enums.StockStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemResponse(
    Long id,
    String name,
    String code,
    String description,
    ItemCategory category,
    ItemSubCategory subCategory,
    String departmentName,
    ItemUnit unit,
    BigDecimal currentStock,
    BigDecimal reorderLevel,
    BigDecimal minStockLevel,
    String location,
    BigDecimal lastPurchaseRate,
    BigDecimal avgRate,
    BigDecimal stockValue,
    StockStatus stockStatus,
    String hsnCode,
    BigDecimal gstRate,
    Boolean isActive,
    Boolean isScrap,
    LocalDateTime createdAt
) {}
