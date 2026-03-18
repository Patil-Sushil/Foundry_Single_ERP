package com.kalibyte.foundry.production.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.order.repository.OrderRepository;
import com.kalibyte.foundry.production.dto.response.report.daily.DailyOrderEntry;
import com.kalibyte.foundry.production.dto.response.report.daily.DailyProductionReport;
import com.kalibyte.foundry.production.dto.response.report.monthly.MonthlyDaySummary;
import com.kalibyte.foundry.production.dto.response.report.monthly.MonthlyProductionReport;
import com.kalibyte.foundry.production.dto.response.report.orderwise.OrderItemProgress;
import com.kalibyte.foundry.production.dto.response.report.orderwise.OrderProductionReport;
import com.kalibyte.foundry.production.dto.response.report.summary.ProductionDashboardSummary;
import com.kalibyte.foundry.production.repository.ProductionEntryRepository;
import com.kalibyte.foundry.production.repository.ProductionItemRepository;
import com.kalibyte.foundry.production.service.ProductionReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductionReportServiceImpl implements ProductionReportService {

    private final OrderRepository orderRepo;
    private final ProductionEntryRepository entryRepo;
    private final ProductionItemRepository itemRepo;

    //------------------------------------------------
    // ORDER REPORT
    //------------------------------------------------

    @Override
    public OrderProductionReport getOrderReport(UUID orderId) {

        Order order = orderRepo.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<OrderItemProgress> items = new ArrayList<>();

        int totalDispatch = 0;
        int totalOrdered = 0;

        for (OrderItem item : order.getItems()) {

            Object[] totals = itemRepo.getPipelineTotals(item.getId());

            int dispatched = ((Number) totals[4]).intValue();

            totalDispatch += dispatched;
            totalOrdered += item.getQuantity();

            items.add(OrderItemProgress.builder()
                    .itemName(item.getProductName())
                    .patternNumber(item.getPattern() != null
                            ? item.getPattern().getPatternNumber()
                            : null)
                    .orderedQuantity(item.getQuantity())
                    .totalReadyCores(((Number) totals[0]).intValue())
                    .totalPouredMoulds(((Number) totals[1]).intValue())
                    .totalShotBlasting(((Number) totals[2]).intValue())
                    .totalFettling(((Number) totals[3]).intValue())
                    .totalDispatched(dispatched)
                    .pendingDispatch(item.getQuantity() - dispatched)
                    .build());
        }

        return OrderProductionReport.builder()
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomer().getName())
                .totalOrderedQuantity(totalOrdered)
                .totalProduced(totalDispatch)
                .totalDispatched(totalDispatch)
                .pendingDispatch(totalOrdered - totalDispatch)
                .items(items)
                .build();
    }

    //------------------------------------------------
    // DAILY REPORT
    //------------------------------------------------

    @Override
    public DailyProductionReport getDailyReport(LocalDate date) {

        var entries = entryRepo.findByDate(date);

        List<DailyOrderEntry> orders = new ArrayList<>();

        int totalProd = 0;
        int totalDispatch = 0;

        for (var entry : entries) {

            totalProd += entry.getTotalFettlingQuantity();
            totalDispatch += entry.getTotalDispatchedQuantity();

            orders.add(DailyOrderEntry.builder()
                    .orderNumber(entry.getOrder().getOrderNumber())
                    .customerName(entry.getOrder().getCustomer().getName())
                    .produced(entry.getTotalFettlingQuantity())
                    .dispatched(entry.getTotalDispatchedQuantity())
                    .build());
        }

        return DailyProductionReport.builder()
                .date(date)
                .totalProduction(totalProd)
                .totalDispatch(totalDispatch)
                .orders(orders)
                .build();
    }

    //------------------------------------------------
    // MONTHLY REPORT
    //------------------------------------------------

    @Override
    public MonthlyProductionReport getMonthlyReport(int month, int year) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        var entries = entryRepo.findByDateRange(start, end);

        Map<LocalDate, MonthlyDaySummary> map = new HashMap<>();

        int totalProd = 0;
        int totalDispatch = 0;

        for (var entry : entries) {

            LocalDate date = entry.getReportDate();

            map.putIfAbsent(date, MonthlyDaySummary.builder()
                    .date(date)
                    .produced(0)
                    .dispatched(0)
                    .build());

            MonthlyDaySummary day = map.get(date);

            day.setProduced(day.getProduced() + entry.getTotalFettlingQuantity());
            day.setDispatched(day.getDispatched() + entry.getTotalDispatchedQuantity());

            totalProd += entry.getTotalFettlingQuantity();
            totalDispatch += entry.getTotalDispatchedQuantity();
        }

        return MonthlyProductionReport.builder()
                .month(month)
                .year(year)
                .totalProduction(totalProd)
                .totalDispatch(totalDispatch)
                .dailyData(new ArrayList<>(map.values()))
                .build();
    }

    //------------------------------------------------
    // DASHBOARD
    //------------------------------------------------

    @Override
    public ProductionDashboardSummary getDashboardSummary() {

        LocalDate today = LocalDate.now();

        var todayEntries = entryRepo.findByDate(today);

        int todayProd = todayEntries.stream()
                .mapToInt(e -> e.getTotalFettlingQuantity())
                .sum();

        int todayDispatch = todayEntries.stream()
                .mapToInt(e -> e.getTotalDispatchedQuantity())
                .sum();

        LocalDate startMonth = today.withDayOfMonth(1);

        var monthEntries = entryRepo.findByDateRange(startMonth, today);

        int monthProd = monthEntries.stream()
                .mapToInt(e -> e.getTotalFettlingQuantity())
                .sum();

        int monthDispatch = monthEntries.stream()
                .mapToInt(e -> e.getTotalDispatchedQuantity())
                .sum();

        return ProductionDashboardSummary.builder()
                .todayProduction(todayProd)
                .todayDispatch(todayDispatch)
                .monthProduction(monthProd)
                .monthDispatch(monthDispatch)
                .totalPendingDispatch(0) // can enhance later
                .activeOrders(0) // can enhance later
                .build();
    }
}
