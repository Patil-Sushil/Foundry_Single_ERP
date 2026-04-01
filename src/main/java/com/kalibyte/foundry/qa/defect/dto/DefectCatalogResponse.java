package com.kalibyte.foundry.qa.defect.dto;

import com.kalibyte.foundry.qa.common.enums.DefectCategory;
import com.kalibyte.foundry.qa.common.enums.Severity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DefectCatalogResponse {
    private Long id;
    private String code;
    private String name;
    private DefectCategory category;
    private Severity severity;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
}
