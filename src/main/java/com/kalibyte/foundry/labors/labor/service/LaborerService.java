package com.kalibyte.foundry.labors.labor.service;

import com.kalibyte.foundry.labors.labor.dto.LaborerRequest;
import com.kalibyte.foundry.labors.labor.dto.LaborerResponse;

import java.util.List;

public interface LaborerService {
    LaborerResponse createLaborer(LaborerRequest request);
    List<LaborerResponse> getAllLaborers();
    LaborerResponse getLaborerById(Long id);
    LaborerResponse updateLaborer(Long id, LaborerRequest request);
    LaborerResponse deleteLaborer(Long id);
}
