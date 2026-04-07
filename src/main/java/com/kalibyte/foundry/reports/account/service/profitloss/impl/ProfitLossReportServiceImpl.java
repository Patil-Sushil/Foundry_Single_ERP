package com.kalibyte.foundry.reports.account.service.profitloss.impl;

import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.expenses.entity.enums.ExpenseCategory;
import com.kalibyte.foundry.expenses.repository.ExpenseRepository;
import com.kalibyte.foundry.furnace.furnace_heats.entity.ElectricityRate;
import com.kalibyte.foundry.furnace.furnace_heats.entity.FurnaceHeats;
import com.kalibyte.foundry.furnace.furnace_heats.repository.ElectricityRateRepository;
import com.kalibyte.foundry.furnace.furnace_heats.repository.FurnaceHeatsRepository;
import com.kalibyte.foundry.inventory.issue.repository.MaterialIssueRepository;
import com.kalibyte.foundry.labors.attendance.repository.AttendanceRepository;
import com.kalibyte.foundry.labors.payout.repository.WeeklyPayoutRepository;
import com.kalibyte.foundry.payment.repository.PaymentRepository;
import com.kalibyte.foundry.production.repository.ProductionEntryRepository;
import com.kalibyte.foundry.reports.account.dto.response.profitloss.*;
import com.kalibyte.foundry.reports.account.service.profitloss.ProfitLossReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfitLossReportServiceImpl implements ProfitLossReportService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final ProductionEntryRepository productionEntryRepository;
    private final AttendanceRepository attendanceRepository;
    private final MaterialIssueRepository materialIssueRepository;
    private final FurnaceHeatsRepository furnaceHeatsRepository;
    private final ElectricityRateRepository electricityRateRepository;
    private final WeeklyPayoutRepository weeklyPayoutRepository;

    @Override
    @Cacheable(value = "report_profit_loss", key = "(#from == null ? 'today' : #from) + '_' + (#to == null ? 'today' : #to)")
    public ProfitLossReport generateReport(LocalDate from, LocalDate to) {
        // Default to today if dates are not provided
        if (from == null) from = LocalDate.now();
        if (to == null) to = LocalDate.now();

        // 1. REVENUE (Realized + Unrealized WIP)
        BigDecimal totalRevenue = safe(invoiceRepository.getRevenue(from, to));
        BigDecimal collections = safe(paymentRepository.getCollections(from, to));
        BigDecimal wipValue = safe(productionEntryRepository.getProductionValue(from, to));

        // 2. MANUFACTURING COSTS (Direct)
        BigDecimal furnaceMaterialCost = safe(furnaceHeatsRepository.getTotalMaterialCost(from, to));

        BigDecimal laborCost = safe(attendanceRepository.getTotalLaborCost(from, to));

        BigDecimal electricityCost = calculateElectricityCost(from, to);

        // 3. OTHER OPERATIONAL COSTS
        BigDecimal generalMaterialIssueCost = safe(materialIssueRepository.getTotalNonFurnaceIssue(from, to, "FUR"));
        BigDecimal generalExpenses = safe(expenseRepository.getTotalExpenses(from, to));

        // 4. AGGREGATED PROFIT CALCULATION
        BigDecimal totalIncome = totalRevenue.add(wipValue);
        BigDecimal cogs = furnaceMaterialCost.add(electricityCost).add(laborCost).add(generalMaterialIssueCost);

        BigDecimal grossProfit = totalIncome.subtract(cogs);
        BigDecimal netProfit = grossProfit.subtract(generalExpenses);

        BigDecimal grossMargin = percent(grossProfit, totalIncome);
        BigDecimal netMargin = percent(netProfit, totalIncome);
        BigDecimal expenseRatio = percent(generalExpenses, totalIncome);

        ProfitLossSummary summary = new ProfitLossSummary(
                totalRevenue,
                collections,
                wipValue,
                furnaceMaterialCost,
                electricityCost,
                laborCost,
                generalMaterialIssueCost,
                grossProfit,
                generalExpenses,
                netProfit,
                grossMargin,
                netMargin,
                expenseRatio
        );

        List<ProfitLossExpenseItem> expenseItems = buildExpenseBreakdown(totalIncome, from, to);
        List<ProfitLossMonthlyItem> monthlyTrend = buildMonthlyTrend(from, to);

        return new ProfitLossReport(
                summary,
                expenseItems,
                monthlyTrend,
                LocalDateTime.now(),
                "SYSTEM"
        );
    }

    /**
     * Calculates electricity cost by applying the correct historical rate
     * for each heat based on when it occurred.
     */
    private BigDecimal calculateElectricityCost(LocalDate from, LocalDate to) {
        List<FurnaceHeats> heats = furnaceHeatsRepository.findHeatsInDateRange(from, to);
        if (heats.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Pre-fetch all applicable rates for the date range (optimization)
        List<ElectricityRate> applicableRates = electricityRateRepository.findRatesEffectiveBetween(from, to);
        
        // Fallback: get current active rate if no historical rates found
        Double fallbackRate = electricityRateRepository.findByActiveTrue()
                .map(ElectricityRate::getRatePerUnit)
                .orElse(0.0);

        BigDecimal totalCost = BigDecimal.ZERO;

        for (FurnaceHeats heat : heats) {
            // Get the heat's date from the associated furnace report
            LocalDate heatDate = (heat.getFurnace() != null) ? heat.getFurnace().getDate() : null;
            
            // Handle null date edge case
            if (heatDate == null) {
                heatDate = from; // Use report start date as fallback
            }

            // Find the applicable rate for this heat's date
            Double rateForHeat = findApplicableRate(heatDate, applicableRates, fallbackRate);

            // Calculate cost for this heat
            double unitsConsumed = heat.getDifferenceReading();
            BigDecimal heatCost = BigDecimal.valueOf(unitsConsumed * rateForHeat);

            totalCost = totalCost.add(heatCost);
        }

        return totalCost.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Finds the applicable electricity rate for a specific date
     * from a pre-fetched list of rates.
     */
    private Double findApplicableRate(LocalDate date, List<ElectricityRate> rates, Double fallbackRate) {
        for (ElectricityRate rate : rates) {
            boolean afterOrOnStart = !date.isBefore(rate.getEffectiveFrom());
            boolean beforeOrOnEnd = rate.getEffectiveTo() == null || !date.isAfter(rate.getEffectiveTo());
            
            if (afterOrOnStart && beforeOrOnEnd) {
                return rate.getRatePerUnit();
            }
        }
        
        // No matching rate found — use fallback
        return fallbackRate;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal percent(BigDecimal value, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        return value
                .divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private List<ProfitLossExpenseItem> buildExpenseBreakdown(BigDecimal revenue, LocalDate from, LocalDate to) {
        List<Object[]> rows = expenseRepository.getExpenseBreakdown(from, to);
        List<ProfitLossExpenseItem> list = new ArrayList<>();

        for (Object[] r : rows) {
            Object categoryObj = r[0];
            String category;

            if (categoryObj instanceof String) {
                category = (String) categoryObj;
            } else if (categoryObj instanceof ExpenseCategory) {
                category = ((ExpenseCategory) categoryObj).name();
            } else {
                category = "UNKNOWN";
            }

            String subCategory = r[1] != null ? r[1].toString() : "-";
            BigDecimal amount = safe((BigDecimal) r[2]);

            list.add(new ProfitLossExpenseItem(category, subCategory, amount, percent(amount, revenue)));
        }
        return list;
    }

    private List<ProfitLossMonthlyItem> buildMonthlyTrend(LocalDate from, LocalDate to) {
        List<ProfitLossMonthlyItem> result = new ArrayList<>();
        YearMonth startMonth = YearMonth.from(from);
        YearMonth endMonth = YearMonth.from(to);
        BigDecimal previousIncome = BigDecimal.ZERO;

        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
            LocalDate start = month.atDay(1);
            LocalDate end = month.atEndOfMonth();
            LocalDate calculationStart = start.isBefore(from) ? from : start;
            LocalDate calculationEnd = end.isAfter(to) ? to : end;

            BigDecimal revenue = safe(invoiceRepository.getRevenue(calculationStart, calculationEnd));
            BigDecimal monthlyWip = safe(productionEntryRepository.getProductionValue(calculationStart, calculationEnd));
            BigDecimal monthlyIncome = revenue.add(monthlyWip);
            
            BigDecimal monthlyCogs = safe(furnaceHeatsRepository.getTotalMaterialCost(calculationStart, calculationEnd))
                    .add(calculateElectricityCost(calculationStart, calculationEnd))
                    .add(safe(attendanceRepository.getTotalLaborCost(calculationStart, calculationEnd)))
                    .add(safe(materialIssueRepository.getTotalNonFurnaceIssue(calculationStart, calculationEnd, "FUR")));
            
            BigDecimal monthlyExp = safe(expenseRepository.getTotalExpenses(calculationStart, calculationEnd));
            BigDecimal netProfit = monthlyIncome.subtract(monthlyCogs).subtract(monthlyExp);

            BigDecimal growth = BigDecimal.ZERO;
            if (previousIncome.compareTo(BigDecimal.ZERO) > 0) {
                growth = monthlyIncome.subtract(previousIncome).divide(previousIncome, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            }

            result.add(new ProfitLossMonthlyItem(month, monthlyIncome, monthlyCogs, monthlyExp, netProfit, growth));
            previousIncome = monthlyIncome;
        }
        return result;
    }
}
