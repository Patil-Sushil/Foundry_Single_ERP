package com.kalibyte.foundry.common.castingprocess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CastingProcessResponse {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private Boolean active;
}
