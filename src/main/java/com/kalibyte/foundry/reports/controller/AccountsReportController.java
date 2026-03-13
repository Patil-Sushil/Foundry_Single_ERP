package com.kalibyte.foundry.reports.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.reports.dto.response.accounts.DailyCollectionReport;
import com.kalibyte.foundry.reports.service.AccountsReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports/accounts")
@RequiredArgsConstructor
public class AccountsReportController {

    private final AccountsReportService accountsReportService;

    @GetMapping("/daily-collection")
    public ApiResponse<DailyCollectionReport> dailyCollection(

            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ){

        return ApiResponse.success(
                accountsReportService.getDailyCollection(from, to)
        );
    }
}