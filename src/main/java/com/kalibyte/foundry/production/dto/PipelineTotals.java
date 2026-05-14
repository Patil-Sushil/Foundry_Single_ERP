package com.kalibyte.foundry.production.dto;


public record PipelineTotals(
        int totalReadyCores,
        int totalPouredMoulds,
        int totalShotBlasting,
        int totalFettling,
        int totalDispatched,
        int totalRejected,
        int totalInspected,
        int totalAccepted
) {
    public static final PipelineTotals ZERO = new PipelineTotals(0, 0, 0, 0, 0, 0, 0, 0);
}
