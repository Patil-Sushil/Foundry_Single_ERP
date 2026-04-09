package com.kalibyte.foundry.reports.gst.service.impl;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.reports.gst.dto.request.GstReportRequest;
import com.kalibyte.foundry.billing.creditnote.entity.CreditNote;
import com.kalibyte.foundry.billing.creditnote.repository.CreditNoteRepository;
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
    private final CreditNoteRepository creditNoteRepository;

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
        List<CreditNote> creditNotes = creditNoteRepository.findOutwardCreditNotes(from, to);

        // Group by GSTIN
        Map<String, List<Invoice>> groupedInvoices = invoices.stream()
                .collect(Collectors.groupingBy(inv -> inv.getCustomer().getGstNumber().trim()));
        Map<String, List<CreditNote>> groupedCNs = creditNotes.stream()
                .collect(Collectors.groupingBy(cn -> cn.getCustomer().getGstNumber().trim()));

        Set<String> allGstins = new TreeSet<>();
        allGstins.addAll(groupedInvoices.keySet());
        allGstins.addAll(groupedCNs.keySet());

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;

        BigDecimal totalCnTaxable = BigDecimal.ZERO;
        BigDecimal totalCnGst = BigDecimal.ZERO;
        BigDecimal totalCnValue = BigDecimal.ZERO;

        List<B2BCustomerGroup> customerGroups = new ArrayList<>();

        for (String gstin : allGstins) {
            List<Invoice> custInvoices = groupedInvoices.getOrDefault(gstin, Collections.emptyList());
            List<CreditNote> custCNs = groupedCNs.getOrDefault(gstin, Collections.emptyList());

            String customerName = !custInvoices.isEmpty() ? custInvoices.get(0).getCustomer().getName()
                    : custCNs.get(0).getCustomer().getName();

            BigDecimal grpTaxable = BigDecimal.ZERO;
            BigDecimal grpGst = BigDecimal.ZERO;
            BigDecimal grpValue = BigDecimal.ZERO;

            List<B2BInvoiceItem> invItems = new ArrayList<>();
            for (Invoice inv : custInvoices) {
                invItems.add(mapToB2BItem(inv));
                grpTaxable = grpTaxable.add(safe(inv.getSubtotal()));
                grpGst = grpGst.add(safe(inv.getTotalGst()));
                grpValue = grpValue.add(safe(inv.getTotalAmount()));
            }

            BigDecimal grpCnTaxable = BigDecimal.ZERO;
            BigDecimal grpCnGst = BigDecimal.ZERO;
            BigDecimal grpCnValue = BigDecimal.ZERO;

            List<B2BCreditNoteItem> cnItems = new ArrayList<>();
            for (CreditNote cn : custCNs) {
                cnItems.add(mapToB2BCreditNoteItem(cn));
                grpCnTaxable = grpCnTaxable.add(safe(cn.getSubtotal()));
                grpCnGst = grpCnGst.add(safe(cn.getTotalGst()));
                grpCnValue = grpCnValue.add(safe(cn.getTotalAmount()));
            }

            customerGroups.add(B2BCustomerGroup.builder()
                    .gstin(gstin)
                    .customerName(customerName)
                    .invoiceCount(custInvoices.size())
                    .totalTaxableValue(grpTaxable)
                    .totalGst(grpGst)
                    .totalInvoiceValue(grpValue)
                    .totalCreditNoteTaxableValue(grpCnTaxable)
                    .totalCreditNoteGst(grpCnGst)
                    .totalCreditNoteValue(grpCnValue)
                    .invoices(invItems)
                    .creditNotes(cnItems)
                    .build());

            totalTaxable = totalTaxable.add(grpTaxable);
            totalGst = totalGst.add(grpGst);
            totalValue = totalValue.add(grpValue);

            totalCnTaxable = totalCnTaxable.add(grpCnTaxable);
            totalCnGst = totalCnGst.add(grpCnGst);
            totalCnValue = totalCnValue.add(grpCnValue);
        }

        // Net Totals
        for (Invoice inv : invoices) {
            totalCgst = totalCgst.add(safe(inv.getCgst()));
            totalSgst = totalSgst.add(safe(inv.getSgst()));
            totalIgst = totalIgst.add(safe(inv.getIgst()));
        }
        
        BigDecimal totalCnCgst = BigDecimal.ZERO;
        BigDecimal totalCnSgst = BigDecimal.ZERO;
        BigDecimal totalCnIgst = BigDecimal.ZERO;
        for (CreditNote cn : creditNotes) {
            totalCnCgst = totalCnCgst.add(safe(cn.getCgst()));
            totalCnSgst = totalCnSgst.add(safe(cn.getSgst()));
            totalCnIgst = totalCnIgst.add(safe(cn.getIgst()));
        }

        return Gstr1B2BReport.builder()
                .periodFrom(from)
                .periodTo(to)
                .periodDescription(periodDesc)
                .totalCustomers(customerGroups.size())
                .totalInvoices(invoices.size())
                .totalCreditNoteCount(creditNotes.size())
                .totalTaxableValue(totalTaxable)
                .totalCgst(totalCgst)
                .totalSgst(totalSgst)
                .totalIgst(totalIgst)
                .totalGst(totalGst)
                .totalInvoiceValue(totalValue)
                .totalCreditNoteTaxableValue(totalCnTaxable)
                .totalCreditNoteGst(totalCnGst)
                .totalCreditNoteValue(totalCnValue)
                .netTaxableValue(totalTaxable.subtract(totalCnTaxable))
                .netGst(totalGst.subtract(totalCnGst))
                .customerGroups(customerGroups)
                .build();
    }

    private B2BCreditNoteItem mapToB2BCreditNoteItem(CreditNote cn) {
        return B2BCreditNoteItem.builder()
                .creditNoteId(cn.getId())
                .creditNoteNumber(cn.getCreditNoteNumber())
                .issueDate(cn.getIssueDate())
                .originalInvoiceNumber(cn.getOriginalInvoiceNumber())
                .gstType(cn.getGstType())
                .taxableValue(safe(cn.getSubtotal()))
                .gstRate(safe(cn.getGstPercentage()))
                .cgstAmount(safe(cn.getCgst()))
                .sgstAmount(safe(cn.getSgst()))
                .igstAmount(safe(cn.getIgst()))
                .totalGst(safe(cn.getTotalGst()))
                .totalAmount(safe(cn.getTotalAmount()))
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

        List<Object[]> invoiceHsnData = gstInvoiceRepository.getHsnSummaryData(from, to);
        List<Object[]> creditNoteHsnData = creditNoteRepository.getCreditNoteHsnSummary(from, to);

        Map<String, HsnSummaryItem> hsnMap = new LinkedHashMap<>();

        // Process Invoices
        for (Object[] row : invoiceHsnData) {
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

            String key = (materialGrade != null ? materialGrade : "N/A") + "_" + (partName != null ? partName : "N/A") + "_" + gstRate;
            hsnMap.put(key, HsnSummaryItem.builder()
                    .hsnCode(materialGrade != null ? materialGrade : "N/A")
                    .description(partName != null ? partName : "N/A")
                    .uqc("KGS")
                    .totalQuantity(qty)
                    .totalWeight(weight)
                    .taxableValue(amount)
                    .gstRate(gstRate)
                    .cgstAmount(cgst)
                    .sgstAmount(sgst)
                    .igstAmount(igst)
                    .totalGst(gstAmount)
                    .totalValue(amount.add(gstAmount))
                    .build());
        }

        // Process Credit Notes (Subtract)
        for (Object[] row : creditNoteHsnData) {
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

            String key = (materialGrade != null ? materialGrade : "N/A") + "_" + (partName != null ? partName : "N/A") + "_" + gstRate;
            HsnSummaryItem item = hsnMap.get(key);
            if (item != null) {
                item.setTotalQuantity(item.getTotalQuantity().subtract(qty));
                if (item.getTotalWeight() != null) item.setTotalWeight(item.getTotalWeight().subtract(weight));
                item.setTaxableValue(item.getTaxableValue().subtract(amount));
                item.setCgstAmount(item.getCgstAmount().subtract(cgst));
                item.setSgstAmount(item.getSgstAmount().subtract(sgst));
                item.setIgstAmount(item.getIgstAmount().subtract(igst));
                item.setTotalGst(item.getTotalGst().subtract(gstAmount));
                item.setTotalValue(item.getTaxableValue().add(item.getTotalGst()));
            } else {
                // If only CN exists for this HSN in period (rare but possible)
                hsnMap.put(key, HsnSummaryItem.builder()
                        .hsnCode(materialGrade != null ? materialGrade : "N/A")
                        .description(partName != null ? partName : "N/A")
                        .uqc("KGS")
                        .totalQuantity(qty.negate())
                        .totalWeight(weight.negate())
                        .taxableValue(amount.negate())
                        .gstRate(gstRate)
                        .cgstAmount(cgst.negate())
                        .sgstAmount(sgst.negate())
                        .igstAmount(igst.negate())
                        .totalGst(gstAmount.negate())
                        .totalValue(amount.add(gstAmount).negate())
                        .build());
            }
        }

        List<HsnSummaryItem> items = new ArrayList<>(hsnMap.values());
        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;

        for (HsnSummaryItem item : items) {
            totalTaxable = totalTaxable.add(item.getTaxableValue());
            totalCgst = totalCgst.add(item.getCgstAmount());
            totalSgst = totalSgst.add(item.getSgstAmount());
            totalIgst = totalIgst.add(item.getIgstAmount());
            totalGst = totalGst.add(item.getTotalGst());
            totalValue = totalValue.add(item.getTotalValue());
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

        List<CreditNote> outwardCNs = creditNoteRepository.findOutwardCreditNotes(from, to);
        BigDecimal totalCnTaxable = BigDecimal.ZERO;
        BigDecimal totalCnTax = BigDecimal.ZERO;
        
        for (CreditNote cn : outwardCNs) {
            totalCnTaxable = totalCnTaxable.add(safe(cn.getSubtotal()));
            totalCnTax = totalCnTax.add(safe(cn.getTotalGst()));
        }

        // Monthly breakdown
        List<Object[]> monthlyData = gstInvoiceRepository.getMonthlyGstBreakdown(from, to);
        List<MonthlyTaxBreakdown> monthlyBreakdown = new ArrayList<>();

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        Map<String, BigDecimal[]> monthlyCNMap = new HashMap<>();
        for (CreditNote cn : outwardCNs) {
            String monthKey = cn.getIssueDate().withDayOfMonth(1).format(monthFormatter);
            BigDecimal[] values = monthlyCNMap.getOrDefault(monthKey, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            values[0] = values[0].add(safe(cn.getSubtotal()));
            values[1] = values[1].add(safe(cn.getTotalGst()));
            monthlyCNMap.put(monthKey, values);
        }

        for (Object[] row : monthlyData) {
            LocalDate monthDate;
            if (row[0] instanceof Timestamp ts) {
                monthDate = ts.toLocalDateTime().toLocalDate();
            } else {
                monthDate = (LocalDate) row[0];
            }
            
            String monthKey = monthDate.format(monthFormatter);
            BigDecimal cnTaxable = BigDecimal.ZERO;
            BigDecimal cnGst = BigDecimal.ZERO;
            if (monthlyCNMap.containsKey(monthKey)) {
                cnTaxable = monthlyCNMap.get(monthKey)[0];
                cnGst = monthlyCNMap.get(monthKey)[1];
            }

            monthlyBreakdown.add(MonthlyTaxBreakdown.builder()
                    .month(monthKey)
                    .invoiceCount(((Number) row[1]).intValue())
                    .taxableValue(toBigDecimal(row[2]).subtract(cnTaxable))
                    .cgst(toBigDecimal(row[3])) // Simplified
                    .sgst(toBigDecimal(row[4]))
                    .igst(toBigDecimal(row[5]))
                    .totalTax(toBigDecimal(row[6]).subtract(cnGst))
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
                .totalCreditNoteTaxableValue(totalCnTaxable)
                .totalCreditNoteTax(totalCnTax)
                .netTaxableValue(totalTaxable.subtract(totalCnTaxable))
                .netGstLiability(totalOutputTax.subtract(totalCnTax))
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