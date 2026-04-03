package com.kalibyte.foundry.dashboard.service.impl;

import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.dashboard.dto.response.*;
import com.kalibyte.foundry.dashboard.service.DashboardService;
import com.kalibyte.foundry.dashboard.util.DateRangeResolver;
import com.kalibyte.foundry.furnace.furnace_heats.repository.FurnaceHeatsRepository;
import com.kalibyte.foundry.inventory.inward.repository.MaterialInwardRepository;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import com.kalibyte.foundry.order.repository.OrderRepository;
import com.kalibyte.foundry.payment.repository.PaymentRepository;
import com.kalibyte.foundry.production.repository.ProductionEntryRepository;
import com.kalibyte.foundry.qa.inspection.repository.QaInspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final FurnaceHeatsRepository furnaceHeatsRepository;
    private final ItemRepository itemRepository;
    private final MaterialInwardRepository materialInwardRepository;
    private final ProductionEntryRepository productionEntryRepository;
    private final QaInspectionRepository qaInspectionRepository;

    @Override
    @Cacheable(value = "dashboardSummary", key = "#range.startDate().toString() + '-' + #range.endDate().toString()")
    public DashboardSummaryResponse getSummary(DateRangeResolver.DateRange range) {
        DateRangeResolver.DateRange prevRange = calculatePreviousPeriod(range);

        BigDecimal periodRevenue = invoiceRepository.getTotalRevenue(range.startDate(), range.endDate());
        BigDecimal prevPeriodRevenue = invoiceRepository.getTotalRevenue(prevRange.startDate(), prevRange.endDate());

        Object[] rejectionStats = qaInspectionRepository.sumRejectionStatsBetweenDates(range.startDate(), range.endDate());
        BigDecimal rejectionRate = calculateRejectionRate(rejectionStats);

        return DashboardSummaryResponse.builder()
                .periodStartDate(range.startDate())
                .periodEndDate(range.endDate())
                .periodLabel(generatePeriodLabel(range))
                .newOrdersCount(orderRepository.countOrdersBetweenDates(range.startDate(), range.endDate()))
                .newOrdersValue(orderRepository.sumOrderValueBetweenDates(range.startDate(), range.endDate()))
                .periodRevenue(periodRevenue)
                .previousPeriodRevenue(prevPeriodRevenue)
                .heatCount(furnaceHeatsRepository.countHeatsBetweenDates(range.startDate(), range.endDate()))
                .averageMeltingEfficiency(furnaceHeatsRepository.averagePowerToWeightBetweenDates(range.startDate(), range.endDate()))
                .furnaceYieldPercentage(calculateFurnaceYield(range))
                .totalReceivables(invoiceRepository.sumTotalReceivables())
                .overdueInvoicesCount(invoiceRepository.countOverdueInvoices(LocalDate.now()))
                .overdueInvoicesValue(invoiceRepository.sumOverdueInvoicesValue(LocalDate.now()))
                .totalCollections(paymentRepository.getTotalCollection(range.startDate(), range.endDate()))
                .lowStockAlertsCount(itemRepository.countLowStockItems())
                .rejectionRatePercentage(rejectionRate)
                .build();
    }

    @Override
    public SalesInsights getSalesInsights(DateRangeResolver.DateRange range) {
        DateRangeResolver.DateRange prevRange = calculatePreviousPeriod(range);

        BigDecimal periodRevenue = invoiceRepository.getTotalRevenue(range.startDate(), range.endDate());
        BigDecimal prevPeriodRevenue = invoiceRepository.getTotalRevenue(prevRange.startDate(), prevRange.endDate());
        
        BigDecimal growth = BigDecimal.ZERO;
        if (prevPeriodRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growth = periodRevenue.subtract(prevPeriodRevenue)
                    .divide(prevPeriodRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        List<Object[]> topCustRows = orderRepository.findTopCustomersByOrderValueBetweenDates(
                range.startDate(), range.endDate(), PageRequest.of(0, 5));
        
        List<SalesInsights.CustomerSummary> topCustomers = topCustRows.stream()
                .map(row -> SalesInsights.CustomerSummary.builder()
                        .customerId(row[0].toString())
                        .customerName(row[1].toString())
                        .totalOrderValue((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());

        List<Object[]> statusRows = orderRepository.countOrdersByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusRows) {
            statusMap.put(row[0].toString(), (Long) row[1]);
        }

        return SalesInsights.builder()
                .periodStartDate(range.startDate())
                .periodEndDate(range.endDate())
                .periodLabel(generatePeriodLabel(range))
                .newOrdersCount(orderRepository.countOrdersBetweenDates(range.startDate(), range.endDate()))
                .newOrdersValue(orderRepository.sumOrderValueBetweenDates(range.startDate(), range.endDate()))
                .directOrdersCount(orderRepository.countDirectOrdersBetweenDates(range.startDate(), range.endDate()))
                .quotationOrdersCount(orderRepository.countQuotationOrdersBetweenDates(range.startDate(), range.endDate()))
                .periodRevenue(periodRevenue)
                .previousPeriodRevenue(prevPeriodRevenue)
                .revenueGrowthPercentage(growth)
                .top5Customers(topCustomers)
                .orderPipelineStatus(statusMap)
                .build();
    }

    @Override
    public ProductionKPIs getProductionKPIs(DateRangeResolver.DateRange range) {
        Object[] stageData = productionEntryRepository.sumStageQuantitiesBetweenDates(range.startDate(), range.endDate());
        Map<String, Long> bottlenecks = new HashMap<>();
        if (stageData != null && stageData.length >= 4) {
            bottlenecks.put("CORES", ((Number) stageData[0]).longValue());
            bottlenecks.put("MOULDS", ((Number) stageData[1]).longValue());
            bottlenecks.put("SHOT_BLASTING", ((Number) stageData[2]).longValue());
            bottlenecks.put("FETTLING", ((Number) stageData[3]).longValue());
        }

        return ProductionKPIs.builder()
                .periodStartDate(range.startDate())
                .periodEndDate(range.endDate())
                .periodLabel(generatePeriodLabel(range))
                .heatCount(furnaceHeatsRepository.countHeatsBetweenDates(range.startDate(), range.endDate()))
                .averagePowerToWeightRatio(furnaceHeatsRepository.averagePowerToWeightBetweenDates(range.startDate(), range.endDate()))
                .furnaceYieldPercentage(calculateFurnaceYield(range))
                .liquidMetalWeight(furnaceHeatsRepository.sumLiquidMetalWeightBetweenDates(range.startDate(), range.endDate()))
                .totalChargeWeight(furnaceHeatsRepository.sumTotalChargeWeightBetweenDates(range.startDate(), range.endDate()))
                .stageWiseBottlenecks(bottlenecks)
                .dispatchedQuantity(productionEntryRepository.sumDispatchedQuantityBetweenDates(range.startDate(), range.endDate()))
                .scheduledTarget(BigDecimal.ZERO)
                .dispatchPerformancePercentage(BigDecimal.ZERO)
                .build();
    }

    @Override
    public FinancialHealth getFinancialHealth(DateRangeResolver.DateRange range) {
        Object[] taxSummary = invoiceRepository.getOutputTaxSummary(range.startDate(), range.endDate());
        BigDecimal cgst = BigDecimal.ZERO, sgst = BigDecimal.ZERO, igst = BigDecimal.ZERO, salesVal = BigDecimal.ZERO;
        if (taxSummary != null && taxSummary.length >= 4) {
            cgst = (BigDecimal) taxSummary[0];
            sgst = (BigDecimal) taxSummary[1];
            igst = (BigDecimal) taxSummary[2];
            salesVal = (BigDecimal) taxSummary[3];
        }

        BigDecimal materialCost = furnaceHeatsRepository.getTotalMaterialCost(range.startDate(), range.endDate());
        BigDecimal costRatio = BigDecimal.ZERO;
        if (salesVal.compareTo(BigDecimal.ZERO) > 0) {
            costRatio = materialCost.divide(salesVal, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        return FinancialHealth.builder()
                .periodStartDate(range.startDate())
                .periodEndDate(range.endDate())
                .periodLabel(generatePeriodLabel(range))
                .totalReceivables(invoiceRepository.sumTotalReceivables())
                .overdueInvoicesCount(invoiceRepository.countOverdueInvoices(LocalDate.now()))
                .overdueInvoicesValue(invoiceRepository.sumOverdueInvoicesValue(LocalDate.now()))
                .totalCollections(paymentRepository.getTotalCollection(range.startDate(), range.endDate()))
                .periodCgst(cgst)
                .periodSgst(sgst)
                .periodIgst(igst)
                .periodTotalTaxLiability(cgst.add(sgst).add(igst))
                .periodMaterialCost(materialCost)
                .periodSalesValue(salesVal)
                .materialCostRatioPercentage(costRatio)
                .build();
    }

    @Override
    public InventoryAlerts getInventoryAlerts(DateRangeResolver.DateRange range) {
        List<Item> lowStockItemsRaw = itemRepository.findLowStockItems(PageRequest.of(0, 10));
        List<InventoryAlerts.LowStockItem> lowStockItems = lowStockItemsRaw.stream()
                .map(item -> InventoryAlerts.LowStockItem.builder()
                        .itemId(item.getId())
                        .itemName(item.getName())
                        .currentStock(item.getCurrentStock())
                        .reorderLevel(item.getReorderLevel())
                        .unit(item.getUnit().name())
                        .build())
                .collect(Collectors.toList());

        List<Object[]> topVendorRows = materialInwardRepository.findTopVendorsByProcurementValueBetweenDates(
                range.startDate(), range.endDate(), PageRequest.of(0, 5));
        
        List<InventoryAlerts.VendorSummary> topVendors = topVendorRows.stream()
                .map(row -> InventoryAlerts.VendorSummary.builder()
                        .vendorId((Long) row[0])
                        .vendorName(row[1].toString())
                        .mtdProcurementValue((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());

        BigDecimal scrapGenerated = furnaceHeatsRepository.sumScrapGeneratedBetweenDates(range.startDate(), range.endDate());
        BigDecimal scrapReturned = materialInwardRepository.sumScrapReturnedBetweenDates(range.startDate(), range.endDate());

        return InventoryAlerts.builder()
                .periodStartDate(range.startDate())
                .periodEndDate(range.endDate())
                .periodLabel(generatePeriodLabel(range))
                .lowStockItems(lowStockItems)
                .periodProcurementValue(materialInwardRepository.sumProcurementValueBetweenDates(range.startDate(), range.endDate()))
                .scrapGenerated(scrapGenerated)
                .scrapRemelted(scrapReturned)
                .scrapNetValue(scrapGenerated.subtract(scrapReturned))
                .top5Vendors(topVendors)
                .build();
    }

    private DateRangeResolver.DateRange calculatePreviousPeriod(DateRangeResolver.DateRange range) {
        long daysBetween = ChronoUnit.DAYS.between(range.startDate(), range.endDate()) + 1;
        LocalDate prevEndDate = range.startDate().minusDays(1);
        LocalDate prevStartDate = prevEndDate.minusDays(daysBetween - 1);
        return new DateRangeResolver.DateRange(prevStartDate, prevEndDate);
    }

    private String generatePeriodLabel(DateRangeResolver.DateRange range) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        if (range.startDate().equals(today) && range.endDate().equals(today)) {
            return "Today";
        }
        if (range.startDate().equals(yesterday) && range.endDate().equals(yesterday)) {
            return "Yesterday";
        }
        
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        if (range.startDate().equals(firstOfMonth) && range.endDate().equals(today)) {
            return "This Month";
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return range.startDate().format(fmt) + " - " + range.endDate().format(fmt);
    }

    private BigDecimal calculateFurnaceYield(DateRangeResolver.DateRange range) {
        BigDecimal liquidMetal = furnaceHeatsRepository.sumLiquidMetalWeightBetweenDates(range.startDate(), range.endDate());
        BigDecimal chargeWeight = furnaceHeatsRepository.sumTotalChargeWeightBetweenDates(range.startDate(), range.endDate());
        if (chargeWeight.compareTo(BigDecimal.ZERO) > 0) {
            return liquidMetal.divide(chargeWeight, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateRejectionRate(Object[] stats) {
        if (stats != null && stats.length >= 2) {
            BigDecimal inspected = new BigDecimal(((Number) stats[0]).toString());
            BigDecimal rejected = new BigDecimal(((Number) stats[1]).toString());
            if (inspected.compareTo(BigDecimal.ZERO) > 0) {
                return rejected.divide(inspected, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            }
        }
        return BigDecimal.ZERO;
    }
}
