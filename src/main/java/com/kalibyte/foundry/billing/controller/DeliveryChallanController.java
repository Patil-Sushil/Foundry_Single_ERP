package com.kalibyte.foundry.billing.controller;

import com.kalibyte.foundry.billing.dto.request.DeliveryChallanRequest;
import com.kalibyte.foundry.billing.dto.response.DeliveryChallanResponse;
import com.kalibyte.foundry.billing.service.DeliveryChallanService;
import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/delivery-challans")
@RequiredArgsConstructor
public class DeliveryChallanController {

    private final DeliveryChallanService deliveryChallanService;

    //------------------------------------------------
    // CREATE DC
    //------------------------------------------------

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryChallanResponse>> create(
            @Valid @RequestBody DeliveryChallanRequest request) {

        DeliveryChallanResponse response =
                deliveryChallanService.createDeliveryChallan(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    //------------------------------------------------
    // GET DC BY ID
    //------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryChallanResponse>> getById(
            @PathVariable UUID id
    ) {

        DeliveryChallanResponse response =
                deliveryChallanService.getDeliveryChallan(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    //------------------------------------------------
    // LIST DCs (PAGINATION)
    //------------------------------------------------

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DeliveryChallanResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                ApiResponse.success(
                        deliveryChallanService.list(pageable)
                )
        );
    }

    //------------------------------------------------
    // DISPATCH DC
    //------------------------------------------------

    @PatchMapping("/{id}/dispatch")
    public ResponseEntity<ApiResponse<DeliveryChallanResponse>> dispatch(
            @PathVariable UUID id
    ) {

        DeliveryChallanResponse response =
                deliveryChallanService.dispatchDeliveryChallan(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    //------------------------------------------------
    // DOWNLOAD DELIVERY CHALLAN PDF
    //------------------------------------------------

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {

        byte[] pdf = deliveryChallanService.generateDeliveryChallanPdf(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=delivery-challan-" + id + ".pdf")
                .body(pdf);
    }
}