package com.kalibyte.foundry.pattern.dto.request;

import com.kalibyte.foundry.pattern.entity.ENUMS.PatternMaterial;
import com.kalibyte.foundry.pattern.entity.ENUMS.PatternType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatternCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    private PatternType type;

    @NotNull
    private PatternMaterial material;
}
