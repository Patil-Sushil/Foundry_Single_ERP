package com.kalibyte.foundry.qa.defect.dto;

import com.kalibyte.foundry.qa.common.enums.DefectCategory;
import com.kalibyte.foundry.qa.common.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DefectCatalogRequest {
    @NotBlank(message = "Code is required")
    private String code;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotNull(message = "Category is required")
    private DefectCategory category;
    
    @NotNull(message = "Severity is required")
    private Severity severity;
    
    private String description;
    private Boolean isActive = true;
}
