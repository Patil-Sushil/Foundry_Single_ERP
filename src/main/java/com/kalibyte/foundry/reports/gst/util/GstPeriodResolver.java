package com.kalibyte.foundry.reports.gst.util;

import com.kalibyte.foundry.reports.gst.dto.request.GstReportRequest;
import com.kalibyte.foundry.reports.gst.entity.enums.GstPeriodType;

import java.time.format.DateTimeFormatter;

public final class GstPeriodResolver {

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private GstPeriodResolver() {}

    public static String describe(GstReportRequest request) {
        return switch (request.getPeriodType()) {
            case MONTHLY -> String.format("%s %d",
                    java.time.Month.of(request.getMonth()).name(), request.getYear());

            case QUARTERLY -> String.format("Q%d FY %d-%d",
                    request.getQuarter(), request.getYear(),
                    request.getQuarter() == 4 ? request.getYear() + 1 : request.getYear());

            case YEARLY -> String.format("FY %d-%d",
                    request.getFinancialYear(), request.getFinancialYear() + 1);

            case CUSTOM -> String.format("%s to %s",
                    request.getFromDate().format(DISPLAY_FMT),
                    request.getToDate().format(DISPLAY_FMT));
        };
    }

    public static String filenameSuffix(GstReportRequest request) {
        return switch (request.getPeriodType()) {
            case MONTHLY -> String.format("%d_%02d", request.getYear(), request.getMonth());
            case QUARTERLY -> String.format("Q%d_%d", request.getQuarter(), request.getYear());
            case YEARLY -> String.format("FY%d_%d",
                    request.getFinancialYear(), request.getFinancialYear() + 1);
            case CUSTOM -> String.format("%s_to_%s",
                    request.getFromDate().toString(), request.getToDate().toString());
        };
    }
}