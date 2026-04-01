package com.kalibyte.foundry.reports.gst.service.impl;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.reports.gst.dto.request.GstReportRequest;
import com.kalibyte.foundry.reports.gst.dto.response.b2b.*;
import com.kalibyte.foundry.reports.gst.dto.response.b2c.*;
import com.kalibyte.foundry.reports.gst.dto.response.document.*;
import com.kalibyte.foundry.reports.gst.dto.response.hsn.*;
import com.kalibyte.foundry.reports.gst.dto.response.salesregister.*;
import com.kalibyte.foundry.reports.gst.dto.response.taxliability.*;
import com.kalibyte.foundry.reports.gst.repository.GstInvoiceRepository;
import com.kalibyte.foundry.reports.gst.service.GstOutwardReportService;
import com.kalibyte.foundry.reports.gst.util.GstPeriodResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GstOutwardReportServiceImpl implements GstOutwardReportService {

    private final GstInvoiceRepository gstInvoiceRepository;

    // ================================================
    // B2B REPORT
    // ================================================
    @Override
    public Gstr1B2BReport generateB2BReport(GstReportRequest request) {
        validateRequest(request);

        LocalDate from = request.resolvedFromDate();
        LocalDate to = request.resolvedToDate();
        String periodDesc = GstPeriodResolver.describe(request);

        List<Invoice> invoices = gstInvoiceRepository.findB2BInvoices(from, to);

        // Group by GSTIN
        Map<String, List<Invoice>> groupedByGstin = invoices.stream()
                .collect(Collectors.groupingBy(
                        inv -> inv.getCustomer().getGstNumber().trim(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;

        List<B2BCustomerGroup> customerGroups = new ArrayList<>();

        for (Map.Entry<String, List<Invoice>> entry : groupedByGstin.entrySet()) {
            String gstin = entry.getKey();
            List<Invoice> custInvoices = entry.getValue();
            String customerName = custInvoices.get(0).getCustomer().getName();

            BigDecimal grpTaxable = BigDecimal.ZERO;
            BigDecimal grpGst = BigDecimal.ZERO;
            BigDecimal grpValue = BigDecimal.ZERO;

            List<B2BInvoiceItem> items = new ArrayList<>();

            for (Invoice inv : custInvoices) {
                B2BInvoiceItem item = mapToB2BItem(inv);
                items.add(item);

                grpTaxable = grpTaxable.add(safe(inv.getSubtotal()));
                grpGst = grpGst.add(safe(inv.getTotalGst()));
                grpValue = grpValue.add(safe(inv.getTotalAmount()));
            }

            customerGroups.add(B2BCustomerGroup.builder()
                    .gstin(gstin)
                    .customerName(customerName)
                    .invoiceCount(custInvoices.size())
                    .totalTaxableValue(grpTaxable)
                    .totalGst(grpGst)
                    .totalInvoiceValue(grpValue)
                    .invoices(items)
                    .build());

            totalTaxable = totalTaxable.add(grpTaxable);
            totalGst = totalGst.add(grpGst);
            totalValue = totalValue.add(grpValue);
        }

        // Calculate total CGST/SGST/IGST
        for (Invoice inv : invoices) {
            totalCgst = totalCgst.add(safe(inv.getCgst()));
            totalSgst = totalSgst.add(safe(inv.getSgst()));
            totalIgst = totalIgst.add(safe(inv.getIgst()));
        }

        return Gstr1B2BReport.builder()
                .periodFrom(from)
                .periodTo(to)
                .periodDescription(periodDesc)
                .totalCustomers(customerGroups.size())
                .totalInvoices(invoices.size())
                .totalTaxableValue(totalTaxable)
                .totalCgst(totalCgst)
                .totalSgst(totalSgst)
                .totalIgst(totalIgst)
                .totalGst(totalGst)
                .totalInvoiceValue(totalValue)
                .customerGroups(customerGroups)
                .build();
    }

    // ================================================
    // B2C LARGE REPORT
    // ================================================
    @Override
    public Gstr1B2CReport generateB2CLargeReport(GstReportRequest request) {
        validateRequest(request);

        LocalDate from = request.resolvedFromDate();
        LocalDate to = request.resolvedToDate();

        List<Invoice> invoices = gstInvoiceRepository.findB2CLargeInvoices(from, to);

        return buildB2CReport(invoices, from, to,
                GstPeriodResolver.describe(request), "B2C_LARGE");
    }

    // ================================================
    // B2C SMALL REPORT
    // ================================================
    @Override
    public Gstr1B2CReport generateB2CSmallReport(GstReportRequest request) {
        validateRequest(request);

        LocalDate from = request.resolvedFromDate();
        LocalDate to = request.resolvedToDate();

        List<Invoice> invoices = gstInvoiceRepository.findB2CSmallInvoices(from, to);

        return buildB2CReport(invoices, from, to,
                GstPeriodResolver.describe(request), "B2C_SMALL");
    }

    // ================================================
    // HSN SUMMARY
    // ================================================
    @Override
    public HsnSummaryReport generateHsnSummary(GstReportRequest request) {
        validateRequest(request);

        LocalDate from = request.resolvedFromDate();
        LocalDate to = request.resolvedToDate();

        List<Object[]> rawData = gstInvoiceRepository.getHsnSummaryData(from, to);

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;

        List<HsnSummaryItem> items = new ArrayList<>();

        for (Object[] row : rawData) {
            String materialGrade = (String) row[0];
            String partName = (String) row[1];
            BigDecimal qty = toBigDecimal(row[2]);
            BigDecimal weight = toBigDecimal(row[3]);
            BigDecimal amount = toBigDecimal(row[4]);
            BigDecimal gstRate = toBigDecimal(row[5]);
            BigDecimal gstAmount = toBigDecimal(row[6]);
            BigDecimal cgst = toBigDecimal(row[7]);
            BigDecimal sgst = toBigDecimal(row[8]);
            BigDecimal igst = toBigDecimal(row[9]);

            items.add(HsnSummaryItem.builder()
                    .hsnCode(materialGrade != null ? materialGrade : "N/A")
                    .description(partName != null ? partName : "N/A")
                    .uqc("KGS")
                    .totalQuantity(qty)
                    .totalValue(amount.add(gstAmount))
                    .taxableValue(amount)
                    .gstRate(gstRate)
                    .cgstAmount(cgst)
                    .sgstAmount(sgst)
                    .igstAmount(igst)
                    .totalGst(gstAmount)
                    .build());

            totalTaxable = totalTaxable.add(amount);
            totalCgst = totalCgst.add(cgst);
            totalSgst = totalSgst.add(sgst);
            totalIgst = totalIgst.add(igst);
            totalGst = totalGst.add(gstAmount);
            totalValue = totalValue.add(amount.add(gstAmount));
        }

        return HsnSummaryReport.builder()
                .periodFrom(from)
                .periodTo(to)
                .periodDescription(GstPeriodResolver.describe(request))
                .totalHsnCodes(items.size())
                .totalTaxableValue(totalTaxable)
                .totalCgst(totalCgst)
                .totalSgst(totalSgst)
                .totalIgst(totalIgst)
                .totalGst(totalGst)
                .totalInvoiceValue(totalValue)
                .items(items)
                .build();
    }

    // ================================================
    // DOCUMENT SUMMARY
    // ================================================
    @Override
    public DocumentSummaryReport generateDocumentSummary(GstReportRequest request) {
        validateRequest(request);

        LocalDate from = request.resolvedFromDate();
        LocalDate to = request.resolvedToDate();

        // ================================================
        // FIXED: Use List<Object[]> — always returns 1 row
        // Row: [minInvoiceNumber, maxInvoiceNumber, count]
        // ================================================
        List<Object[]> results = gstInvoiceRepository.getActiveInvoiceSummary(from, to);
        Long cancelledCount = gstInvoiceRepository.getCancelledInvoiceCount(from, to);

        String fromSerial = "N/A";
        String toSerial = "N/A";
        int totalActive = 0;

        if (results != null && !results.isEmpty()) {
            Object[] row = results.get(0);
            fromSerial = row[0] != null ? row[0].toString() : "N/A";
            toSerial = row[1] != null ? row[1].toString() : "N/A";
            totalActive = row[2] != null ? ((Number) row[2]).intValue() : 0;
        }

        int totalCancelled = cancelledCount != null ? cancelledCount.intValue() : 0;
        int totalIssued = totalActive + totalCancelled;

        DocumentSummaryItem invoiceDoc = DocumentSummaryItem.builder()
                .documentType("Invoices")
                .fromSerialNo(fromSerial)
                .toSerialNo(toSerial)
                .totalIssued(totalIssued)
                .totalCancelled(totalCancelled)
                .netIssued(totalActive)
                .build();

        return DocumentSummaryReport.builder()
                .periodFrom(from)
                .periodTo(to)
                .periodDescription(GstPeriodResolver.describe(request))
                .totalDocumentsIssued(totalIssued)
                .totalCancelled(totalCancelled)
                .netIssued(totalActive)
                .items(List.of(invoiceDoc))
                .build();
    }

    // ================================================
    // SALES REGISTER
    // ================================================
    @Override
    public SalesRegisterReport generateSalesRegister(GstReportRequest request) {
        validateRequest(request);

        LocalDate from = request.resolvedFromDate();
        LocalDate to = request.resolvedToDate();

        List<Invoice> invoices = gstInvoiceRepository.findAllInvoicesForPeriod(from, to);

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;

        List<SalesRegisterItem> items = new ArrayList<>();

        for (Invoice inv : invoices) {
            Customer customer = inv.getCustomer();
            Order order = inv.getOrder();

            items.add(SalesRegisterItem.builder()
                    .invoiceId(inv.getId())
                    .invoiceNumber(inv.getInvoiceNumber())
                    .invoiceDate(inv.getInvoiceDate())
                    .dueDate(inv.getDueDate())
                    .customerName(customer.getName())
                    .companyName(customer.getCompanyName())
                    .gstin(customer.getGstNumber())
                    .state(customer.getState())
                    .placeOfSupply(order.getPlaceOfSupply())
                    .orderNumber(order.getOrderNumber())
                    .orderType(order.getOrderType() != null ? order.getOrderType().name() : null)
                    .taxableValue(safe(inv.getSubtotal()))
                    .gstType(inv.getGstType())
                    .gstRate(safe(inv.getGstPercentage()))
                    .cgstAmount(safe(inv.getCgst()))
                    .sgstAmount(safe(inv.getSgst()))
                    .igstAmount(safe(inv.getIgst()))
                    .totalGst(safe(inv.getTotalGst()))
                    .invoiceValue(safe(inv.getTotalAmount()))
                    .invoiceStatus(inv.getBillStatus() != null ? inv.getBillStatus().name() : null)
                    .paymentStatus(inv.getBillStatus() != null ? inv.getBillStatus().name() : null)
                    .build());

            totalTaxable = totalTaxable.add(safe(inv.getSubtotal()));
            totalCgst = totalCgst.add(safe(inv.getCgst()));
            totalSgst = totalSgst.add(safe(inv.getSgst()));
            totalIgst = totalIgst.add(safe(inv.getIgst()));
            totalGst = totalGst.add(safe(inv.getTotalGst()));
            totalValue = totalValue.add(safe(inv.getTotalAmount()));
        }

        return SalesRegisterReport.builder()
                .periodFrom(from)
                .periodTo(to)
                .periodDescription(GstPeriodResolver.describe(request))
                .totalInvoices(invoices.size())
                .totalTaxableValue(totalTaxable)
                .totalCgst(totalCgst)
                .totalSgst(totalSgst)
                .totalIgst(totalIgst)
                .totalGst(totalGst)
                .totalInvoiceValue(totalValue)
                .items(items)
                .build();
    }

    // ================================================
    // TAX LIABILITY SUMMARY
    // ================================================
    @Override
    public TaxLiabilitySummary generateTaxLiabilitySummary(GstReportRequest request) {
        validateRequest(request);

        LocalDate from = request.resolvedFromDate();
        LocalDate to = request.resolvedToDate();

        List<Invoice> allInvoices = gstInvoiceRepository.findAllInvoicesForPeriod(from, to);

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;

        int b2bCount = 0;
        BigDecimal b2bTaxable = BigDecimal.ZERO;
        BigDecimal b2bTax = BigDecimal.ZERO;

        int b2cCount = 0;
        BigDecimal b2cTaxable = BigDecimal.ZERO;
        BigDecimal b2cTax = BigDecimal.ZERO;

        for (Invoice inv : allInvoices) {
            totalTaxable = totalTaxable.add(safe(inv.getSubtotal()));
            totalCgst = totalCgst.add(safe(inv.getCgst()));
            totalSgst = totalSgst.add(safe(inv.getSgst()));
            totalIgst = totalIgst.add(safe(inv.getIgst()));

            Customer customer = inv.getCustomer();
            boolean hasGstin = customer.getGstNumber() != null
                    && !customer.getGstNumber().trim().isEmpty();

            if (hasGstin) {
                b2bCount++;
                b2bTaxable = b2bTaxable.add(safe(inv.getSubtotal()));
                b2bTax = b2bTax.add(safe(inv.getTotalGst()));
            } else {
                b2cCount++;
                b2cTaxable = b2cTaxable.add(safe(inv.getSubtotal()));
                b2cTax = b2cTax.add(safe(inv.getTotalGst()));
            }
        }

        // Monthly breakdown
        List<Object[]> monthlyData = gstInvoiceRepository.getMonthlyGstBreakdown(from, to);
        List<MonthlyTaxBreakdown> monthlyBreakdown = new ArrayList<>();

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (Object[] row : monthlyData) {
            LocalDate monthDate;
            if (row[0] instanceof Timestamp ts) {
                monthDate = ts.toLocalDateTime().toLocalDate();
            } else {
                monthDate = (LocalDate) row[0];
            }

            monthlyBreakdown.add(MonthlyTaxBreakdown.builder()
                    .month(monthDate.format(monthFormatter))
                    .invoiceCount(((Number) row[1]).intValue())
                    .taxableValue(toBigDecimal(row[2]))
                    .cgst(toBigDecimal(row[3]))
                    .sgst(toBigDecimal(row[4]))
                    .igst(toBigDecimal(row[5]))
                    .totalTax(toBigDecimal(row[6]))
                    .build());
        }

        BigDecimal totalOutputTax = totalCgst.add(totalSgst).add(totalIgst);

        return TaxLiabilitySummary.builder()
                .periodFrom(from)
                .periodTo(to)
                .periodDescription(GstPeriodResolver.describe(request))
                .totalTaxableValue(totalTaxable)
                .totalCgst(totalCgst)
                .totalSgst(totalSgst)
                .totalIgst(totalIgst)
                .totalOutputTax(totalOutputTax)
                .totalB2BInvoices(b2bCount)
                .b2bTaxableValue(b2bTaxable)
                .b2bTax(b2bTax)
                .totalB2CInvoices(b2cCount)
                .b2cTaxableValue(b2cTaxable)
                .b2cTax(b2cTax)
                .monthlyBreakdown(monthlyBreakdown)
                .build();
    }

    // ================================================
    // PRIVATE HELPER METHODS
    // ================================================

    private void validateRequest(GstReportRequest request) {
        if (request == null) {
            throw new BusinessException("Report request cannot be null");
        }

        LocalDate from = request.resolvedFromDate();
        LocalDate to = request.resolvedToDate();

        if (from == null || to == null) {
            throw new BusinessException("Period dates could not be resolved. Check your input.");
        }

        if (to.isBefore(from)) {
            throw new BusinessException("'To' date cannot be before 'From' date");
        }

        // Max 1 year range
        if (from.plusYears(1).isBefore(to)) {
            throw new BusinessException("Report period cannot exceed 1 year");
        }
    }

    private B2BInvoiceItem mapToB2BItem(Invoice inv) {
        Customer customer = inv.getCustomer();
        Order order = inv.getOrder();

        return B2BInvoiceItem.builder()
                .invoiceId(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .invoiceDate(inv.getInvoiceDate())
                .invoiceValue(safe(inv.getTotalAmount()))
                .customerName(customer.getName())
                .gstin(customer.getGstNumber())
                .placeOfSupply(order.getPlaceOfSupply())
                .gstType(inv.getGstType())
                .taxableValue(safe(inv.getSubtotal()))
                .gstRate(safe(inv.getGstPercentage()))
                .cgstAmount(safe(inv.getCgst()))
                .sgstAmount(safe(inv.getSgst()))
                .igstAmount(safe(inv.getIgst()))
                .totalGst(safe(inv.getTotalGst()))
                .reverseCharge("N")
                .build();
    }

    private Gstr1B2CReport buildB2CReport(
            List<Invoice> invoices,
            LocalDate from,
            LocalDate to,
            String periodDesc,
            String type) {

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;

        List<B2CInvoiceItem> items = new ArrayList<>();

        for (Invoice inv : invoices) {
            Customer customer = inv.getCustomer();
            Order order = inv.getOrder();

            items.add(B2CInvoiceItem.builder()
                    .invoiceId(inv.getId())
                    .invoiceNumber(inv.getInvoiceNumber())
                    .invoiceDate(inv.getInvoiceDate())
                    .customerName(customer.getName())
                    .placeOfSupply(order.getPlaceOfSupply())
                    .gstType(inv.getGstType())
                    .taxableValue(safe(inv.getSubtotal()))
                    .gstRate(safe(inv.getGstPercentage()))
                    .cgstAmount(safe(inv.getCgst()))
                    .sgstAmount(safe(inv.getSgst()))
                    .igstAmount(safe(inv.getIgst()))
                    .totalGst(safe(inv.getTotalGst()))
                    .invoiceValue(safe(inv.getTotalAmount()))
                    .build());

            totalTaxable = totalTaxable.add(safe(inv.getSubtotal()));
            totalCgst = totalCgst.add(safe(inv.getCgst()));
            totalSgst = totalSgst.add(safe(inv.getSgst()));
            totalIgst = totalIgst.add(safe(inv.getIgst()));
            totalGst = totalGst.add(safe(inv.getTotalGst()));
            totalValue = totalValue.add(safe(inv.getTotalAmount()));
        }

        return Gstr1B2CReport.builder()
                .periodFrom(from)
                .periodTo(to)
                .periodDescription(periodDesc)
                .type(type)
                .totalInvoices(invoices.size())
                .totalTaxableValue(totalTaxable)
                .totalCgst(totalCgst)
                .totalSgst(totalSgst)
                .totalIgst(totalIgst)
                .totalGst(totalGst)
                .totalInvoiceValue(totalValue)
                .invoices(items)
                .build();
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }
}