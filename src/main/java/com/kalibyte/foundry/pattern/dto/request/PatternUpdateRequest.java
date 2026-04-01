package com.kalibyte.foundry.pattern.dto.request;

import com.kalibyte.foundry.pattern.entity.enums.PatternMaterial;
import com.kalibyte.foundry.pattern.entity.enums.PatternType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatternUpdateRequest {

    private String name;
    private PatternType type;
    private PatternMaterial material;
    private String rackNumber;
}