package com.kalibyte.foundry.pattern.dto.response;

import com.kalibyte.foundry.pattern.entity.enums.PatternMaterial;
import com.kalibyte.foundry.pattern.entity.enums.PatternStatus;
import com.kalibyte.foundry.pattern.entity.enums.PatternType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
@Setter
public class PatternResponse {

    private UUID id;
    private String patternNumber;
    private String name;
    private PatternType type;
    private PatternMaterial material;
    private PatternStatus status;
    private String rackNumber;
}