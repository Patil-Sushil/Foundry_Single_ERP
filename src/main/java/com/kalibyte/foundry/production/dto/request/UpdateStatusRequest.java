package com.kalibyte.foundry.production.dto.request;

import com.kalibyte.foundry.production.entity.enums.ProductionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull
    private ProductionStatus status;
}