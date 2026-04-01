package com.kalibyte.foundry.reports.gst.dto.request;

import com.kalibyte.foundry.reports.gst.entity.enums.GstPeriodType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.YearMonth;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GstReportRequest {

    @NotNull(message = "Period type is required")
    private GstPeriodType periodType;

    // --- For CUSTOM ---
    private LocalDate fromDate;
    private LocalDate toDate;

    // --- For MONTHLY ---
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @Min(value = 2020, message = "Year must be 2020 or later")
    private Integer year;

    // --- For QUARTERLY ---
    @Min(value = 1, message = "Quarter must be between 1 and 4")
    @Max(value = 4, message = "Quarter must be between 1 and 4")
    private Integer quarter;

    // --- For YEARLY (Financial Year) ---
    @Min(value = 2020, message = "Financial year must be 2020 or later")
    private Integer financialYear;

    /**
     * Resolves the actual date range based on period type.
     */
    public LocalDate resolvedFromDate() {
        return switch (periodType) {
            case CUSTOM -> fromDate;
            case MONTHLY -> YearMonth.of(year, month).atDay(1);
            case QUARTERLY -> getQuarterStartDate(quarter, year);
            case YEARLY -> LocalDate.of(financialYear, 4, 1); // FY starts April
        };
    }

    public LocalDate resolvedToDate() {
        return switch (periodType) {
            case CUSTOM -> toDate;
            case MONTHLY -> YearMonth.of(year, month).atEndOfMonth();
            case QUARTERLY -> getQuarterEndDate(quarter, year);
            case YEARLY -> LocalDate.of(financialYear + 1, 3, 31); // FY ends March
        };
    }

    private LocalDate getQuarterStartDate(int quarter, int year) {
        return switch (quarter) {
            case 1 -> LocalDate.of(year, 4, 1);   // Apr-Jun
            case 2 -> LocalDate.of(year, 7, 1);   // Jul-Sep
            case 3 -> LocalDate.of(year, 10, 1);  // Oct-Dec
            case 4 -> LocalDate.of(year + 1, 1, 1); // Jan-Mar
            default -> throw new IllegalArgumentException("Invalid quarter: " + quarter);
        };
    }

    private LocalDate getQuarterEndDate(int quarter, int year) {
        return switch (quarter) {
            case 1 -> LocalDate.of(year, 6, 30);
            case 2 -> LocalDate.of(year, 9, 30);
            case 3 -> LocalDate.of(year, 12, 31);
            case 4 -> LocalDate.of(year + 1, 3, 31);
            default -> throw new IllegalArgumentException("Invalid quarter: " + quarter);
        };
    }

    @AssertTrue(message = "Custom period requires both fromDate and toDate")
    public boolean isCustomDatesValid() {
        if (periodType == GstPeriodType.CUSTOM) {
            return fromDate != null && toDate != null && !toDate.isBefore(fromDate);
        }
        return true;
    }

    @AssertTrue(message = "Monthly period requires month and year")
    public boolean isMonthlyValid() {
        if (periodType == GstPeriodType.MONTHLY) {
            return month != null && year != null;
        }
        return true;
    }

    @AssertTrue(message = "Quarterly period requires quarter and year")
    public boolean isQuarterlyValid() {
        if (periodType == GstPeriodType.QUARTERLY) {
            return quarter != null && year != null;
        }
        return true;
    }

    @AssertTrue(message = "Yearly period requires financialYear")
    public boolean isYearlyValid() {
        if (periodType == GstPeriodType.YEARLY) {
            return financialYear != null;
        }
        return true;
    }
}