package com.kalibyte.foundry.qa.inspection.dto;

import com.kalibyte.foundry.qa.common.enums.FindingDisposition;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FindingResponse {
    private Long id;
    private Long defectId;
    private String defectCode;
    private String defectName;
    private Integer quantityAffected;
    private FindingDisposition disposition;
    private String reworkInstruction;
    private List<String> photoUrls;
    private String remarks;
    private LocalDateTime createdAt;
}
