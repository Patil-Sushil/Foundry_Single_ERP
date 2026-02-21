package com.kalibyte.foundry.pattern.service;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.pattern.dto.request.PatternCreateRequest;
import com.kalibyte.foundry.pattern.dto.response.PatternResponse;

import java.util.UUID;

public interface PatternService {

    PatternResponse create(PatternCreateRequest request);

    PageResponse<PatternResponse> getAll(int page, int size, String sort);

    PatternResponse getById(UUID id);
}
