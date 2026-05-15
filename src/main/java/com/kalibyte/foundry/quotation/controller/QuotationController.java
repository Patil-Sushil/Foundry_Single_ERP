package com.kalibyte.foundry.quotation.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.quotation.dto.request.QuotationCreateRequest;
import com.kalibyte.foundry.quotation.dto.response.QuotationResponse;
import com.kalibyte.foundry.quotation.entity.Quotation;
import com.kalibyte.foundry.quotation.entity.enums.QuotationStatus;
import com.kalibyte.foundry.quotation.mapper.QuotationMapper;
import com.kalibyte.foundry.quotation.service.QuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;
    private final QuotationMapper quotationMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<QuotationResponse>> create(
            @Valid @RequestBody QuotationCreateRequest request
    ) {
        Quotation quotation = quotationService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(quotationMapper.toResponse(quotation)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuotationResponse>> getById(
            @PathVariable UUID id
    ) {
        Quotation quotation = quotationService.get(id);
        return ResponseEntity.ok(ApiResponse.success(quotationMapper.toResponse(quotation)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<QuotationResponse>>> list(
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
                ApiResponse.success(quotationService.list(pageable))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuotationResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody QuotationCreateRequest request
    ) {
        Quotation quotation = quotationService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(quotationMapper.toResponse(quotation)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<QuotationResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestParam QuotationStatus status
    ) {
        Quotation quotation = quotationService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(quotationMapper.toResponse(quotation)));
    }


    // Custom action to send quotation by email
    @PostMapping("/{id}/send/email")
    public ResponseEntity<ApiResponse<QuotationResponse>> sendByEmail(
            @PathVariable UUID id
    ) {
        Quotation quotation = quotationService.sendByEmail(id);
        return ResponseEntity.ok(
                ApiResponse.success(quotationMapper.toResponse(quotation))
        );
    }


}
