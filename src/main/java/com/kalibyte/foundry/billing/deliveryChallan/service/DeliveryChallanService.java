package com.kalibyte.foundry.billing.deliveryChallan.service;

import com.kalibyte.foundry.billing.deliveryChallan.dto.request.DeliveryChallanRequest;
import com.kalibyte.foundry.billing.deliveryChallan.dto.response.DeliveryChallanResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DeliveryChallanService {

    DeliveryChallanResponse createDeliveryChallan(DeliveryChallanRequest request);

    DeliveryChallanResponse getDeliveryChallan(UUID id);

    List<DeliveryChallanResponse> getAllDeliveryChallans();

    DeliveryChallanResponse dispatchDeliveryChallan(UUID id);

    PageResponse<DeliveryChallanResponse> list(Pageable pageable);

    byte[] generateDeliveryChallanPdf(UUID dcId);
}
