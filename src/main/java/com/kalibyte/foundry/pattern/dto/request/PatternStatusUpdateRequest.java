package com.kalibyte.foundry.pattern.dto.request;

import com.kalibyte.foundry.pattern.entity.ENUMS.PatternStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatternStatusUpdateRequest {

    private PatternStatus status;
}