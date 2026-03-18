package com.kalibyte.foundry.production.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PipelineTotals {

    private Integer totalReadyCores;
    private Integer totalPouredMoulds;
    private Integer totalShotBlasting;
    private Integer totalFettling;
    private Integer totalDispatched;
}
