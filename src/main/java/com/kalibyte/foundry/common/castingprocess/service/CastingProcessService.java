package com.kalibyte.foundry.common.castingprocess.service;

import com.kalibyte.foundry.common.castingprocess.dto.CastingProcessRequest;
import com.kalibyte.foundry.common.castingprocess.dto.CastingProcessResponse;
import com.kalibyte.foundry.common.castingprocess.entity.CastingProcessMaster;

import java.util.List;
import java.util.UUID;

public interface CastingProcessService {
    CastingProcessResponse create(CastingProcessRequest request);
    CastingProcessResponse update(UUID id, CastingProcessRequest request);
    CastingProcessResponse get(UUID id);
    List<CastingProcessResponse> getAll();
    List<CastingProcessResponse> getAllActive();
    void delete(UUID id);
    CastingProcessMaster getEntity(UUID id);
}
