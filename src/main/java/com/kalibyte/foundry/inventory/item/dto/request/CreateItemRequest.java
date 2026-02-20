package com.kalibyte.foundry.inventory.item.dto.request;

import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemSubCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public record CreateItemRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Code is required")
    String code,

    String description,

    @NotNull(message = "Category is required")
    ItemCategory category,

    ItemSubCategory subCategory,

    Long departmentId,

    @NotNull(message = "Unit is required")
    ItemUnit unit,

    @DecimalMin(value = "0.0", message = "Reorder level must be >= 0")
    BigDecimal reorderLevel,

    @DecimalMin(value = "0.0", message = "Minimum stock level must be >= 0")
    BigDecimal minStockLevel,

    String location,
    String hsnCode,

    @DecimalMin(value = "0.0", message = "GST rate must be >= 0")
    BigDecimal gstRate
) implements Serializable {}
