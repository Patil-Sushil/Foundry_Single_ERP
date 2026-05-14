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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import com.kalibyte.foundry.production.dto.response.report.dashboard.DelayedOrderResponse;
import com.kalibyte.foundry.production.dto.response.report.dashboard.WipDashboardResponse;
import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import java.time.temporal.ChronoUnit;

@Service
@Transactional(readOnly = true)
public class ProductionReportServiceImpl implements ProductionReportService {

    private final OrderRepository orderRepo;
    private final ProductionEntryRepository entryRepo;
    private final ProductionItemRepository itemRepo;

    public ProductionReportServiceImpl(OrderRepository orderRepo, ProductionEntryRepository entryRepo, ProductionItemRepository itemRepo) {
        this.orderRepo = orderRepo;
        this.entryRepo = entryRepo;
        this.itemRepo = itemRepo;
    }

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
        int totalAccepted = 0;
        int totalCompletionQty = 0;

        int totalWaitingForShotBlast = 0;
        int totalWaitingForFettling = 0;
        int totalWaitingForInspection = 0;

        for (OrderItem item : order.getItems()) {
            PipelineTotals totals = getCumulativeTotals(item.getId());
            OrderItemProgress progress = buildOrderItemProgress(item, totals);
            
            totalDispatched += totals.totalDispatched();
            totalProduced += totals.totalFettling();
            totalRejected += totals.totalRejected();
            totalOrdered += item.getQuantity();
            totalAccepted += totals.totalAccepted();
            totalCompletionQty += (totals.totalAccepted() > 0) ? totals.totalAccepted() : totals.totalFettling();

            totalWaitingForShotBlast += progress.getWaitingForShotBlast();
            totalWaitingForFettling += progress.getWaitingForFettling();
            totalWaitingForInspection += progress.getWaitingForInspection();

            items.add(progress);
        }

        int totalRemaining = totalOrdered - totalAccepted;
        double overallCompletion = totalOrdered > 0 ? (totalCompletionQty * 100.0) / totalOrdered : 0;
        overallCompletion = Math.clamp(overallCompletion, 0, 100.0);

        LocalDate maxEtaDate = items.stream()
                .map(OrderItemProgress::getExpectedCompletionDate)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);

        boolean isDelayed = order.getDeliveryDate() != null && maxEtaDate != null && maxEtaDate.isAfter(order.getDeliveryDate());

        return OrderProductionReport.builder()
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomer().getName())
                .totalOrderedQuantity(totalOrdered)
                .totalProduced(totalProduced)
                .totalDispatched(totalDispatched)
                .totalRejected(totalRejected)
                .pendingDispatch(totalOrdered - totalDispatched)
                .items(items)
                .overallCompletionPercentage(overallCompletion)
                .totalAcceptedQty(totalAccepted)
                .totalRemainingQty(totalRemaining)
                .totalWaitingForShotBlast(totalWaitingForShotBlast)
                .totalWaitingForFettling(totalWaitingForFettling)
                .totalWaitingForInspection(totalWaitingForInspection)
                .expectedCompletionDate(maxEtaDate)
                .delayed(isDelayed)
                .build();
    }

    private OrderItemProgress buildOrderItemProgress(OrderItem item, PipelineTotals totals) {
        int orderedQty = item.getQuantity();
        int acceptedQty = totals.totalAccepted();
        int fettlingQty = totals.totalFettling();
        
        // Fallback logic
        int completionQty = (acceptedQty > 0) ? acceptedQty : fettlingQty;
        int remainingQty = Math.max(0, orderedQty - acceptedQty);

        double completionPercentage = orderedQty > 0 ? (completionQty * 100.0) / orderedQty : 0;
        completionPercentage = Math.clamp(completionPercentage, 0, 100.0);

        int waitingForShotBlast = Math.max(0, totals.totalPouredMoulds() - totals.totalShotBlasting());
        int waitingForFettling = Math.max(0, totals.totalShotBlasting() - totals.totalFettling());
        int waitingForInspection = Math.max(0, totals.totalFettling() - totals.totalInspected());

        // ETA calculation
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        Double avgDailyAccepted = itemRepo.getAverageAcceptedQuantity(item.getId(), sevenDaysAgo);
        
        Integer etaDays = null;
        LocalDate expectedCompletionDate = null;
        if (avgDailyAccepted != null && avgDailyAccepted > 0) {
            etaDays = (int) Math.ceil(remainingQty / avgDailyAccepted);
            expectedCompletionDate = LocalDate.now().plusDays(etaDays);
        }

        boolean delayed = item.getOrder().getDeliveryDate() != null && expectedCompletionDate != null 
                && expectedCompletionDate.isAfter(item.getOrder().getDeliveryDate());

        String status = "NOT_STARTED";
        if (acceptedQty >= orderedQty) {
            status = "COMPLETED";
        } else if (totals.totalReadyCores() > 0 || totals.totalPouredMoulds() > 0 || 
                   totals.totalShotBlasting() > 0 || totals.totalFettling() > 0 || 
                   totals.totalInspected() > 0 || acceptedQty > 0) {
            status = "RUNNING";
        }

        return OrderItemProgress.builder()
                .orderId(item.getOrder().getId())
                .orderNumber(item.getOrder().getOrderNumber())
                .customerName(item.getOrder().getCustomer().getName())
                .itemName(item.getPartName())
                .patternNumber(item.getPattern() != null ? item.getPattern().getPatternNumber() : null)
                .orderedQuantity(orderedQty)
                .orderItemId(item.getId())
                .totalReadyCores(totals.totalReadyCores())
                .totalPouredMoulds(totals.totalPouredMoulds())
                .totalShotBlasting(totals.totalShotBlasting())
                .totalFettling(totals.totalFettling())
                .totalDispatched(totals.totalDispatched())
                .totalRejected(totals.totalRejected())
                .pendingDispatch(orderedQty - totals.totalDispatched())
                .acceptedQty(acceptedQty)
                .inspectedQty(totals.totalInspected())
                .waitingForShotBlast(waitingForShotBlast)
                .waitingForFettling(waitingForFettling)
                .waitingForInspection(waitingForInspection)
                .completionPercentage(completionPercentage)
                .remainingQty(remainingQty)
                .etaDays(etaDays)
                .deliveryDate(item.getOrder().getDeliveryDate())
                .expectedCompletionDate(expectedCompletionDate)
                .delayed(delayed)
                .productionStatus(status)
                .build();
    }

    @Override
    public List<OrderItemProgress> getAllOrderProgress() {
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.IN_PRODUCTION,
                OrderStatus.PARTIALLY_PRODUCED,
                OrderStatus.PRODUCED,
                OrderStatus.PARTIALLY_DISPATCHED
        );
        
        List<Order> activeOrders = orderRepo.findByStatusIn(activeStatuses);
        List<OrderItemProgress> progressList = new ArrayList<>();
        
        for (Order order : activeOrders) {
            for (OrderItem item : order.getItems()) {
                PipelineTotals totals = getCumulativeTotals(item.getId());
                progressList.add(buildOrderItemProgress(item, totals));
            }
        }
        return progressList;
    }

    @Override
    public WipDashboardResponse getWipDashboard() {
        List<Object[]> results = itemRepo.getOverallWipTotals();
        if (results == null || results.isEmpty()) {
            return new WipDashboardResponse(0, 0, 0);
        }
        Object[] raw = results.get(0);
        return WipDashboardResponse.builder()
                .totalWaitingForShotBlast(Math.max(0, toInt(raw[0])))
                .totalWaitingForFettling(Math.max(0, toInt(raw[1])))
                .totalWaitingForInspection(Math.max(0, toInt(raw[2])))
                .build();
    }

    @Override
    public List<DelayedOrderResponse> getDelayedOrders() {
        List<OrderItemProgress> allProgress = getAllOrderProgress();
        
        // Group by orderId to avoid duplicate orders in delayed list
        Map<UUID, DelayedOrderResponse> delayedOrdersMap = new HashMap<>();
        
        for (OrderItemProgress p : allProgress) {
            if (Boolean.TRUE.equals(p.getDelayed())) {
                long delayDays = 0;
                if (p.getExpectedCompletionDate() != null && p.getDeliveryDate() != null) {
                    delayDays = ChronoUnit.DAYS.between(p.getDeliveryDate(), p.getExpectedCompletionDate());
                }
                
                DelayedOrderResponse existing = delayedOrdersMap.get(p.getOrderId());
                if (existing == null || delayDays > existing.getDelayDays()) {
                    // If multiple items delayed, take the one with maximum delay
                    delayedOrdersMap.put(p.getOrderId(), DelayedOrderResponse.builder()
                            .orderId(p.getOrderId())
                            .orderNumber(p.getOrderNumber())
                            .customerName(p.getCustomerName())
                            .deliveryDate(p.getDeliveryDate())
                            .expectedCompletionDate(p.getExpectedCompletionDate())
                            .delayDays(delayDays)
                            .completionPercentage(p.getCompletionPercentage())
                            .build());
                }
            }
        }
        
        return new ArrayList<>(delayedOrdersMap.values());
    }

// ── Add this helper to report service too ──

    private PipelineTotals getCumulativeTotals(UUID orderItemId) {
        List<Object[]> results = itemRepo.getPipelineTotalsRaw(orderItemId);
        if (results == null || results.isEmpty()) {
            return PipelineTotals.ZERO;
        }
        Object[] raw = results.get(0);
        if (raw == null || raw.length < 8) {
            return PipelineTotals.ZERO;
        }
        return new PipelineTotals(
                toInt(raw[0]), toInt(raw[1]), toInt(raw[2]),
                toInt(raw[3]), toInt(raw[4]), toInt(raw[5]),
                toInt(raw[6]), toInt(raw[7])
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