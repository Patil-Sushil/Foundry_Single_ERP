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
        int totalOrdered = 0;

        for (OrderItem item : order.getItems()) {

            // ── FIX: Use List<Object[]> extraction ──
            PipelineTotals totals = getCumulativeTotals(item.getId());

            totalDispatched += totals.totalDispatched();
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
                    item.getQuantity() - totals.totalDispatched()
            ));
        }

        return new OrderProductionReport(
                order.getOrderNumber(),
                order.getCustomer().getName(),
                totalOrdered,
                totalDispatched,
                totalDispatched,
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
        if (raw == null || raw.length < 5) {
            return PipelineTotals.ZERO;
        }
        return new PipelineTotals(
                toInt(raw[0]), toInt(raw[1]), toInt(raw[2]),
                toInt(raw[3]), toInt(raw[4])
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
        List<DailyOrderEntry> orders = new ArrayList<>();

        for (ProductionEntry entry : entries) {
            int produced = entry.getTotalFettlingQuantity();
            int dispatched = entry.getTotalDispatchedQuantity();

            totalProd += produced;
            totalDispatch += dispatched;

            orders.add(new DailyOrderEntry(
                    entry.getOrder().getOrderNumber(),
                    entry.getOrder().getCustomer().getName(),
                    produced,
                    dispatched
            ));
        }

        return new DailyProductionReport(date, totalProd, totalDispatch, orders);
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

        for (ProductionEntry entry : entries) {
            LocalDate date = entry.getReportDate();
            int[] vals = map.computeIfAbsent(date, k -> new int[2]);

            int produced = entry.getTotalFettlingQuantity();
            int dispatched = entry.getTotalDispatchedQuantity();

            vals[0] += produced;
            vals[1] += dispatched;

            totalProd += produced;
            totalDispatch += dispatched;
        }

        List<MonthlyDaySummary> dailyData = map.entrySet().stream()
                .map(e -> new MonthlyDaySummary(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .sorted(Comparator.comparing(MonthlyDaySummary::date))
                .toList();

        return new MonthlyProductionReport(month, year, totalProd, totalDispatch, dailyData);
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

        // month
        List<ProductionEntry> monthEntries = entryRepo.findByDateRangeWithOrder(startOfMonth, today);
        int monthProd = monthEntries.stream()
                .mapToInt(ProductionEntry::getTotalFettlingQuantity).sum();
        int monthDispatch = monthEntries.stream()
                .mapToInt(ProductionEntry::getTotalDispatchedQuantity).sum();

        // active orders & pending dispatch
        int activeOrders = (int) entryRepo.countDistinctOrdersByStatus(ProductionStatus.IN_PROGRESS);
        int pendingDispatch = entryRepo.calculateTotalPendingDispatch();

        return new ProductionDashboardSummary(
                todayProd, todayDispatch,
                monthProd, monthDispatch,
                pendingDispatch,
                activeOrders
        );
    }
}