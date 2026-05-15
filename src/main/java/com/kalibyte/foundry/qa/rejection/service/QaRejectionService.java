package com.kalibyte.foundry.qa.rejection.service;

import com.kalibyte.foundry.qa.common.enums.RejectionDisposition;
import com.kalibyte.foundry.qa.common.enums.RejectionStatus;
import com.kalibyte.foundry.qa.inspection.entity.QaInspection;
import com.kalibyte.foundry.qa.rejection.dto.QaRejectionResponse;

import java.util.List;
import java.util.UUID;

public interface QaRejectionService {
    List<QaRejectionResponse> list(UUID orderId, RejectionStatus status, RejectionDisposition disposition);
    QaRejectionResponse getById(Long id);
    QaRejectionResponse createFromInspection(QaInspection inspection);
    QaRejectionResponse dispositionRejection(Long id, RejectionDisposition disposition, String remarks, String performedBy);
}
