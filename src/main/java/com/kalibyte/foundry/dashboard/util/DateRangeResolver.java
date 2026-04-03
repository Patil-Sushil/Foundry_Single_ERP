package com.kalibyte.foundry.dashboard.util;

import com.kalibyte.foundry.dashboard.dto.request.DateRangePreset;
import com.kalibyte.foundry.dashboard.dto.request.DateRangeRequest;
import com.kalibyte.foundry.dashboard.exception.DashboardException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class DateRangeResolver {

    public record DateRange(LocalDate startDate, LocalDate endDate) {}

    public static DateRange resolve(DateRangeRequest request) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = null;
        LocalDate endDate = now;

        DateRangePreset preset = request.getPreset();

        if (preset == null || preset == DateRangePreset.CUSTOM) {
            startDate = request.getStartDate();
            endDate = request.getEndDate();
            if (startDate == null) startDate = now;
            if (endDate == null) endDate = now;
        } else {
            switch (preset) {
                case TODAY -> {
                    startDate = now;
                    endDate = now;
                }
                case YESTERDAY -> {
                    startDate = now.minusDays(1);
                    endDate = now.minusDays(1);
                }
                case THIS_WEEK -> {
                    startDate = now.with(DayOfWeek.MONDAY);
                    endDate = now;
                }
                case LAST_WEEK -> {
                    startDate = now.minusWeeks(1).with(DayOfWeek.MONDAY);
                    endDate = now.minusWeeks(1).with(DayOfWeek.SUNDAY);
                }
                case THIS_MONTH -> {
                    startDate = now.with(TemporalAdjusters.firstDayOfMonth());
                    endDate = now;
                }
                case LAST_MONTH -> {
                    startDate = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
                    endDate = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                }
                case THIS_QUARTER -> {
                    startDate = getFirstDayOfQuarter(now);
                    endDate = now;
                }
                case LAST_QUARTER -> {
                    LocalDate firstOfThisQuarter = getFirstDayOfQuarter(now);
                    startDate = getFirstDayOfQuarter(firstOfThisQuarter.minusDays(1));
                    endDate = firstOfThisQuarter.minusDays(1);
                }
                case THIS_YEAR -> {
                    startDate = now.with(TemporalAdjusters.firstDayOfYear());
                    endDate = now;
                }
                case LAST_YEAR -> {
                    startDate = now.minusYears(1).with(TemporalAdjusters.firstDayOfYear());
                    endDate = now.minusYears(1).with(TemporalAdjusters.lastDayOfYear());
                }
            }
        }

        if (endDate.isBefore(startDate)) {
            throw new DashboardException("End date cannot be before start date: " + startDate + " to " + endDate);
        }

        return new DateRange(startDate, endDate);
    }

    private static LocalDate getFirstDayOfQuarter(LocalDate date) {
        int month = date.getMonthValue();
        int firstMonthOfQuarter = ((month - 1) / 3) * 3 + 1;
        return LocalDate.of(date.getYear(), firstMonthOfQuarter, 1);
    }
}
