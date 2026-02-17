package com.kalibyte.foundry.quotation.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.quotation.service.QuotationWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/quotations/{id}")
@RequiredArgsConstructor
public class QuotationWorkflowController {


    private final QuotationWorkflowService workflowService;

    @PostMapping("/submit")
    public ApiResponse<String> submit(@PathVariable UUID id) {
        workflowService.submit(id);
        return ApiResponse.success("Quotation submitted successfully");
    }

    @PostMapping("/approve")
    public ApiResponse<String> approve(@PathVariable UUID id) {
        workflowService.approve(id);
        return ApiResponse.success("Quotation approved successfully");
    }
}
