package com.kalibyte.foundry.pattern.dto.response;

import com.kalibyte.foundry.pattern.entity.ENUMS.PatternMaterial;
import com.kalibyte.foundry.pattern.entity.ENUMS.PatternType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class PatternResponse {

    private UUID id;
    private String name;
    private PatternType type;
    private PatternMaterial material;
}