package com.kalibyte.foundry.pattern.dto.request;

import com.kalibyte.foundry.pattern.entity.ENUMS.PatternMaterial;
import com.kalibyte.foundry.pattern.entity.ENUMS.PatternType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PatternReceiptRequest {

    private LocalDate inwardDate;
    private LocalDate outwardDate;
    private String name;
    private PatternType type;
    private PatternMaterial material;
}
