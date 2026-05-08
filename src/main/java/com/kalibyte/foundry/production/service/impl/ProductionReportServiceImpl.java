package com.kalibyte.foundry.production.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.order.repository.OrderRepository;
import com.kalibyte.foundry.production.dto.PipelineTotals;
import com.kalibyte.foundry.production.dto.response.report.daily.DailyOrderEntry;
import com.kalibyte.foundry.production.dto.response.report.daily.DailyProductionReport;
import com.kalibyte.foundry.production.dto.response.report.monthly.MonthlyDaySummary;
import com.kalibyte.foundry.production.dto.response.report.monthly.MonthlyProductionReport;
import com.kalibyte.foundry.production.dto.response.report.orderwise.OrderItemProgress;
import com.kalibyte.foundry.production.dto.response.report.orderwise.OrderProductionReport;
import com.kalibyte.foundry.production.dto.response.report.summary.ProductionDashboardSummary;
import com.kalibyte.foundry.production.entity.ProductionEntry;
import com.kalibyte.foundry.production.entity.enums.ProductionStatus;
import com.kalibyte.foundry.production.repository.ProductionEntryRepository;
import com.kalibyte.foundry.production.repository.ProductionItemRepository;
import com.kalibyte.foundry.production.service.ProductionReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductionReportServiceImpl implements ProductionReportService {

    private final OrderRepository orderRepo;
    private final ProductionEntryRepository entryRepo;
    private final ProductionItemRepository itemRepo;

    // ================================================================
    //  ORDER REPORT
    // ================================================================

    @Override
    public OrderProductionReport getOrderReport(UUID orderId) {

        Order order = orderRepo.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<OrderItemProgress> items = new ArrayList<>();
        int totalDispatched = 0;
        int totalProduced = 0;
        int totalRejected = 0;
        int totalOrdered = 0;

        for (OrderItem item : order.getItems()) {

            // ── FIX: Use List<Object[]> extraction ──
            PipelineTotals totals = getCumulativeTotals(item.getId());

            totalDispatched += totals.totalDispatched();
            totalProduced += totals.totalFettling();
            totalRejected += totals.totalRejected();
            totalOrdered += item.getQuantity();

            items.add(new OrderItemProgress(
                    item.getPartName(),
                    item.getPattern() != null ? item.getPattern().getPatternNumber() : null,
                    item.getQuantity(),
                    item.getId(),
                    totals.totalReadyCores(),
                    totals.totalPouredMoulds(),
                    totals.totalShotBlasting(),
                    totals.totalFettling(),
                    totals.totalDispatched(),
                    totals.totalRejected(),
                    item.getQuantity() - totals.totalDispatched()
            ));
        }

        return new OrderProductionReport(
                order.getOrderNumber(),
                order.getCustomer().getName(),
                totalOrdered,
                totalProduced,
                totalDispatched,
                totalRejected,
                totalOrdered - totalDispatched,
                items
        );
    }

// ── Add this helper to report service too ──

    private PipelineTotals getCumulativeTotals(UUID orderItemId) {
        List<Object[]> results = itemRepo.getPipelineTotalsRaw(orderItemId);
        if (results == null || results.isEmpty()) {
            return PipelineTotals.ZERO;
        }
        Object[] raw = results.get(0);
        if (raw == null || raw.length < 6) {
            return PipelineTotals.ZERO;
        }
        return new PipelineTotals(
                toInt(raw[0]), toInt(raw[1]), toInt(raw[2]),
                toInt(raw[3]), toInt(raw[4]), toInt(raw[5])
        );
    }

    private int toInt(Object value) {
        return value != null ? ((Number) value).intValue() : 0;
    }

    // ================================================================
    //  DAILY REPORT
    // ================================================================

    @Override
    public DailyProductionReport getDailyReport(LocalDate date) {

        List<ProductionEntry> entries = entryRepo.findByDateWithOrder(date);

        int totalProd = 0;
        int totalDispatch = 0;
        int totalRejected = 0;
        List<DailyOrderEntry> orders = new ArrayList<>();

        for (ProductionEntry entry : entries) {
            int produced = entry.getTotalFettlingQuantity();
            int dispatched = entry.getTotalDispatchedQuantity();
            int rejected = entry.getTotalRejectedQuantity();

            totalProd += produced;
            totalDispatch += dispatched;
            totalRejected += rejected;

            orders.add(new DailyOrderEntry(
                    entry.getOrder().getOrderNumber(),
                    entry.getOrder().getCustomer().getName(),
                    produced,
                    dispatched,
                    rejected
            ));
        }

        return new DailyProductionReport(date, totalProd, totalDispatch, totalRejected, orders);
    }

    // ================================================================
    //  MONTHLY REPORT
    // ================================================================

    @Override
    public MonthlyProductionReport getMonthlyReport(int month, int year) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<ProductionEntry> entries = entryRepo.findByDateRangeWithOrder(start, end);

        // aggregate by date using int[] to avoid mutable record issue
        Map<LocalDate, int[]> map = new LinkedHashMap<>();
        int totalProd = 0;
        int totalDispatch = 0;
        int totalRejected = 0;

        for (ProductionEntry entry : entries) {
            LocalDate date = entry.getReportDate();
            int[] vals = map.computeIfAbsent(date, k -> new int[3]);

            int produced = entry.getTotalFettlingQuantity();
            int dispatched = entry.getTotalDispatchedQuantity();
            int rejected = entry.getTotalRejectedQuantity();

            vals[0] += produced;
            vals[1] += dispatched;
            vals[2] += rejected;

            totalProd += produced;
            totalDispatch += dispatched;
            totalRejected += rejected;
        }

        List<MonthlyDaySummary> dailyData = map.entrySet().stream()
                .map(e -> new MonthlyDaySummary(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2]))
                .sorted(Comparator.comparing(MonthlyDaySummary::date))
                .toList();

        return new MonthlyProductionReport(month, year, totalProd, totalDispatch, totalRejected, dailyData);
    }

    // ================================================================
    //  DASHBOARD
    // ================================================================

    @Override
    public ProductionDashboardSummary getDashboardSummary() {

        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);

        // today
        List<ProductionEntry> todayEntries = entryRepo.findByDateWithOrder(today);
        int todayProd = todayEntries.stream()
                .mapToInt(ProductionEntry::getTotalFettlingQuantity).sum();
        int todayDispatch = todayEntries.stream()
                .mapToInt(ProductionEntry::getTotalDispatchedQuantity).sum();
        int todayRejected = todayEntries.stream()
                .mapToInt(ProductionEntry::getTotalRejectedQuantity).sum();

        // month
        List<ProductionEntry> monthEntries = entryRepo.findByDateRangeWithOrder(startOfMonth, today);
        int monthProd = monthEntries.stream()
                .mapToInt(ProductionEntry::getTotalFettlingQuantity).sum();
        int monthDispatch = monthEntries.stream()
                .mapToInt(ProductionEntry::getTotalDispatchedQuantity).sum();
        int monthRejected = monthEntries.stream()
                .mapToInt(ProductionEntry::getTotalRejectedQuantity).sum();

        // active orders & pending dispatch
        int activeOrders = (int) entryRepo.countDistinctOrdersByStatus(ProductionStatus.IN_PROGRESS);
        int pendingDispatch = entryRepo.calculateTotalPendingDispatch();

        return new ProductionDashboardSummary(
                todayProd, todayDispatch, todayRejected,
                monthProd, monthDispatch, monthRejected,
                pendingDispatch,
                activeOrders
        );
    }
}