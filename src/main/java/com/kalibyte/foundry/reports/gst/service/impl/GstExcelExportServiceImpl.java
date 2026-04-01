// src/main/java/com/kalibyte/foundry/reports/gst/service/impl/GstExcelExportServiceImpl.java
package com.kalibyte.foundry.reports.gst.service.impl;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.reports.gst.dto.response.b2b.*;
import com.kalibyte.foundry.reports.gst.dto.response.b2c.*;
import com.kalibyte.foundry.reports.gst.dto.response.hsn.*;
import com.kalibyte.foundry.reports.gst.dto.response.salesregister.*;
import com.kalibyte.foundry.reports.gst.dto.response.taxliability.*;
import com.kalibyte.foundry.reports.gst.service.GstExcelExportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class GstExcelExportServiceImpl implements GstExcelExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String FONT_NAME = "Calibri";

    // ================================================================
    // B2B EXCEL (Government Format)
    // ================================================================
    @Override
    public byte[] exportB2BExcel(Gstr1B2BReport report) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            // --- Sheet 1: B2B Data ---
            Sheet dataSheet = workbook.createSheet("B2B Invoices");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle textStyle = createTextStyle(workbook);

            // Title row
            int rowIdx = 0;
            Row titleRow = dataSheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("GSTR-1 B2B Invoices");
            titleCell.setCellStyle(createTitleStyle(workbook));
            dataSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));

            // Period row
            Row periodRow = dataSheet.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue("Period: " + report.getPeriodDescription());
            dataSheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

            // Empty row
            rowIdx++;

            // Header row
            String[] headers = {
                    "GSTIN/UIN of Recipient", "Receiver Name", "Invoice Number",
                    "Invoice Date", "Invoice Value", "Place of Supply",
                    "Reverse Charge", "Invoice Type", "Rate (%)",
                    "Taxable Value", "CGST Amount", "SGST Amount",
                    "IGST Amount", "Cess Amount"
            };

            Row headerRow = dataSheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (B2BCustomerGroup group : report.getCustomerGroups()) {
                for (B2BInvoiceItem item : group.getInvoices()) {
                    Row row = dataSheet.createRow(rowIdx++);
                    int col = 0;

                    createTextCell(row, col++, item.getGstin(), textStyle);
                    createTextCell(row, col++, item.getCustomerName(), textStyle);
                    createTextCell(row, col++, item.getInvoiceNumber(), textStyle);
                    createDateCell(row, col++, item.getInvoiceDate(), dateStyle);
                    createCurrencyCell(row, col++, item.getInvoiceValue(), currencyStyle);
                    createTextCell(row, col++, item.getPlaceOfSupply(), textStyle);
                    createTextCell(row, col++, item.getReverseCharge(), textStyle);
                    createTextCell(row, col++, "Regular", textStyle);
                    createCurrencyCell(row, col++, item.getGstRate(), currencyStyle);
                    createCurrencyCell(row, col++, item.getTaxableValue(), currencyStyle);
                    createCurrencyCell(row, col++, item.getCgstAmount(), currencyStyle);
                    createCurrencyCell(row, col++, item.getSgstAmount(), currencyStyle);
                    createCurrencyCell(row, col++, item.getIgstAmount(), currencyStyle);
                    createCurrencyCell(row, col++, BigDecimal.ZERO, currencyStyle);
                }
            }

            // Totals row
            Row totalRow = dataSheet.createRow(rowIdx++);
            totalRow.createCell(0).setCellValue("TOTAL");
            totalRow.getCell(0).setCellStyle(headerStyle);
            createCurrencyCell(totalRow, 4, report.getTotalInvoiceValue(), createTotalStyle(workbook));
            createCurrencyCell(totalRow, 9, report.getTotalTaxableValue(), createTotalStyle(workbook));
            createCurrencyCell(totalRow, 10, report.getTotalCgst(), createTotalStyle(workbook));
            createCurrencyCell(totalRow, 11, report.getTotalSgst(), createTotalStyle(workbook));
            createCurrencyCell(totalRow, 12, report.getTotalIgst(), createTotalStyle(workbook));
            createCurrencyCell(totalRow, 13, BigDecimal.ZERO, createTotalStyle(workbook));

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                dataSheet.autoSizeColumn(i);
                // Minimum width
                if (dataSheet.getColumnWidth(i) < 3500) {
                    dataSheet.setColumnWidth(i, 3500);
                }
            }

            // Freeze header
            dataSheet.createFreezePane(0, 4);

            // --- Sheet 2: Summary ---
            createB2BSummarySheet(workbook, report, headerStyle, currencyStyle);

            return toByteArray(workbook);

        } catch (Exception e) {
            log.error("Failed to generate B2B Excel", e);
            throw new BusinessException("Failed to generate B2B Excel report: " + e.getMessage());
        }
    }

    private void createB2BSummarySheet(XSSFWorkbook workbook, Gstr1B2BReport report,
                                       CellStyle headerStyle, CellStyle currencyStyle) {
        Sheet sheet = workbook.createSheet("Summary");
        int rowIdx = 0;

        Row titleRow = sheet.createRow(rowIdx++);
        titleRow.createCell(0).setCellValue("B2B Summary");
        titleRow.getCell(0).setCellStyle(createTitleStyle(workbook));

        rowIdx++; // blank

        String[][] summaryData = {
                {"Period", report.getPeriodDescription()},
                {"Total Customers", String.valueOf(report.getTotalCustomers())},
                {"Total Invoices", String.valueOf(report.getTotalInvoices())},
                {"Total Taxable Value", formatAmount(report.getTotalTaxableValue())},
                {"Total CGST", formatAmount(report.getTotalCgst())},
                {"Total SGST", formatAmount(report.getTotalSgst())},
                {"Total IGST", formatAmount(report.getTotalIgst())},
                {"Total GST", formatAmount(report.getTotalGst())},
                {"Total Invoice Value", formatAmount(report.getTotalInvoiceValue())}
        };

        for (String[] data : summaryData) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(data[0]);
            row.getCell(0).setCellStyle(headerStyle);
            row.createCell(1).setCellValue(data[1]);
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    // ================================================================
    // B2C EXCEL
    // ================================================================
    @Override
    public byte[] exportB2CExcel(Gstr1B2CReport report) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            String sheetName = "B2C_LARGE".equals(report.getType()) ? "B2C Large" : "B2C Small";
            Sheet sheet = workbook.createSheet(sheetName);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle textStyle = createTextStyle(workbook);

            int rowIdx = 0;

            // Title
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.createCell(0).setCellValue("GSTR-1 " + sheetName);
            titleRow.getCell(0).setCellStyle(createTitleStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

            // Period
            Row periodRow = sheet.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue("Period: " + report.getPeriodDescription());
            rowIdx++;

            // Headers
            String[] headers = {
                    "Type", "Place of Supply", "Invoice Number", "Invoice Date",
                    "Invoice Value", "Rate (%)", "Taxable Value",
                    "CGST Amount", "SGST Amount", "IGST Amount", "Cess Amount"
            };

            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            for (B2CInvoiceItem item : report.getInvoices()) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;

                createTextCell(row, col++, report.getType(), textStyle);
                createTextCell(row, col++, item.getPlaceOfSupply(), textStyle);
                createTextCell(row, col++, item.getInvoiceNumber(), textStyle);
                createDateCell(row, col++, item.getInvoiceDate(), dateStyle);
                createCurrencyCell(row, col++, item.getInvoiceValue(), currencyStyle);
                createCurrencyCell(row, col++, item.getGstRate(), currencyStyle);
                createCurrencyCell(row, col++, item.getTaxableValue(), currencyStyle);
                createCurrencyCell(row, col++, item.getCgstAmount(), currencyStyle);
                createCurrencyCell(row, col++, item.getSgstAmount(), currencyStyle);
                createCurrencyCell(row, col++, item.getIgstAmount(), currencyStyle);
                createCurrencyCell(row, col++, BigDecimal.ZERO, currencyStyle);
            }

            // Totals
            CellStyle totalStyle = createTotalStyle(workbook);
            Row totalRow = sheet.createRow(rowIdx);
            totalRow.createCell(0).setCellValue("TOTAL");
            totalRow.getCell(0).setCellStyle(headerStyle);
            createCurrencyCell(totalRow, 4, report.getTotalInvoiceValue(), totalStyle);
            createCurrencyCell(totalRow, 6, report.getTotalTaxableValue(), totalStyle);
            createCurrencyCell(totalRow, 7, report.getTotalCgst(), totalStyle);
            createCurrencyCell(totalRow, 8, report.getTotalSgst(), totalStyle);
            createCurrencyCell(totalRow, 9, report.getTotalIgst(), totalStyle);
            createCurrencyCell(totalRow, 10, BigDecimal.ZERO, totalStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.createFreezePane(0, 4);

            return toByteArray(workbook);

        } catch (Exception e) {
            log.error("Failed to generate B2C Excel", e);
            throw new BusinessException("Failed to generate B2C Excel report: " + e.getMessage());
        }
    }

    // ================================================================
    // HSN SUMMARY EXCEL
    // ================================================================
    @Override
    public byte[] exportHsnSummaryExcel(HsnSummaryReport report) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("HSN Summary");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle textStyle = createTextStyle(workbook);

            int rowIdx = 0;

            // Title
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.createCell(0).setCellValue("GSTR-1 HSN Summary");
            titleRow.getCell(0).setCellStyle(createTitleStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

            Row periodRow = sheet.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue("Period: " + report.getPeriodDescription());
            rowIdx++;

            // Headers
            String[] headers = {
                    "HSN", "Description", "UQC", "Total Quantity",
                    "Total Value", "Taxable Value", "Rate (%)",
                    "CGST Amount", "SGST Amount", "IGST Amount", "Total GST"
            };

            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            for (HsnSummaryItem item : report.getItems()) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;

                createTextCell(row, col++, item.getHsnCode(), textStyle);
                createTextCell(row, col++, item.getDescription(), textStyle);
                createTextCell(row, col++, item.getUqc(), textStyle);
                createCurrencyCell(row, col++, item.getTotalQuantity(), currencyStyle);
                createCurrencyCell(row, col++, item.getTotalValue(), currencyStyle);
                createCurrencyCell(row, col++, item.getTaxableValue(), currencyStyle);
                createCurrencyCell(row, col++, item.getGstRate(), currencyStyle);
                createCurrencyCell(row, col++, item.getCgstAmount(), currencyStyle);
                createCurrencyCell(row, col++, item.getSgstAmount(), currencyStyle);
                createCurrencyCell(row, col++, item.getIgstAmount(), currencyStyle);
                createCurrencyCell(row, col++, item.getTotalGst(), currencyStyle);
            }

            // Totals
            CellStyle totalStyle = createTotalStyle(workbook);
            Row totalRow = sheet.createRow(rowIdx);
            totalRow.createCell(0).setCellValue("TOTAL");
            totalRow.getCell(0).setCellStyle(headerStyle);
            createCurrencyCell(totalRow, 4, report.getTotalInvoiceValue(), totalStyle);
            createCurrencyCell(totalRow, 5, report.getTotalTaxableValue(), totalStyle);
            createCurrencyCell(totalRow, 7, report.getTotalCgst(), totalStyle);
            createCurrencyCell(totalRow, 8, report.getTotalSgst(), totalStyle);
            createCurrencyCell(totalRow, 9, report.getTotalIgst(), totalStyle);
            createCurrencyCell(totalRow, 10, report.getTotalGst(), totalStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.createFreezePane(0, 4);

            return toByteArray(workbook);

        } catch (Exception e) {
            log.error("Failed to generate HSN Excel", e);
            throw new BusinessException("Failed to generate HSN Summary Excel: " + e.getMessage());
        }
    }

    // ================================================================
    // SALES REGISTER EXCEL
    // ================================================================
    @Override
    public byte[] exportSalesRegisterExcel(SalesRegisterReport report) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Sales Register");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle textStyle = createTextStyle(workbook);

            int rowIdx = 0;

            // Title
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.createCell(0).setCellValue("GST Sales Register");
            titleRow.getCell(0).setCellStyle(createTitleStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 17));

            Row periodRow = sheet.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue("Period: " + report.getPeriodDescription());
            rowIdx++;

            // Headers
            String[] headers = {
                    "Invoice Number", "Invoice Date", "Due Date",
                    "Customer Name", "Company Name", "GSTIN",
                    "State", "Place of Supply", "Order Number",
                    "GST Type", "Taxable Value", "GST Rate (%)",
                    "CGST", "SGST", "IGST",
                    "Total GST", "Invoice Value", "Status"
            };

            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            for (SalesRegisterItem item : report.getItems()) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;

                createTextCell(row, col++, item.getInvoiceNumber(), textStyle);
                createDateCell(row, col++, item.getInvoiceDate(), dateStyle);
                createDateCell(row, col++, item.getDueDate(), dateStyle);
                createTextCell(row, col++, item.getCustomerName(), textStyle);
                createTextCell(row, col++, item.getCompanyName(), textStyle);
                createTextCell(row, col++, item.getGstin(), textStyle);
                createTextCell(row, col++, item.getState(), textStyle);
                createTextCell(row, col++, item.getPlaceOfSupply(), textStyle);
                createTextCell(row, col++, item.getOrderNumber(), textStyle);
                createTextCell(row, col++,
                        item.getGstType() != null ? item.getGstType().name() : "", textStyle);
                createCurrencyCell(row, col++, item.getTaxableValue(), currencyStyle);
                createCurrencyCell(row, col++, item.getGstRate(), currencyStyle);
                createCurrencyCell(row, col++, item.getCgstAmount(), currencyStyle);
                createCurrencyCell(row, col++, item.getSgstAmount(), currencyStyle);
                createCurrencyCell(row, col++, item.getIgstAmount(), currencyStyle);
                createCurrencyCell(row, col++, item.getTotalGst(), currencyStyle);
                createCurrencyCell(row, col++, item.getInvoiceValue(), currencyStyle);
                createTextCell(row, col++, item.getInvoiceStatus(), textStyle);
            }

            // Totals
            CellStyle totalStyle = createTotalStyle(workbook);
            Row totalRow = sheet.createRow(rowIdx);
            totalRow.createCell(0).setCellValue("TOTAL (" + report.getTotalInvoices() + " invoices)");
            totalRow.getCell(0).setCellStyle(headerStyle);
            createCurrencyCell(totalRow, 10, report.getTotalTaxableValue(), totalStyle);
            createCurrencyCell(totalRow, 12, report.getTotalCgst(), totalStyle);
            createCurrencyCell(totalRow, 13, report.getTotalSgst(), totalStyle);
            createCurrencyCell(totalRow, 14, report.getTotalIgst(), totalStyle);
            createCurrencyCell(totalRow, 15, report.getTotalGst(), totalStyle);
            createCurrencyCell(totalRow, 16, report.getTotalInvoiceValue(), totalStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.createFreezePane(0, 4);

            // Enable auto-filter
            sheet.setAutoFilter(new CellRangeAddress(3, rowIdx - 1, 0, headers.length - 1));

            return toByteArray(workbook);

        } catch (Exception e) {
            log.error("Failed to generate Sales Register Excel", e);
            throw new BusinessException("Failed to generate Sales Register Excel: " + e.getMessage());
        }
    }

    // ================================================================
    // TAX LIABILITY EXCEL (Multi-sheet)
    // ================================================================
    @Override
    public byte[] exportTaxLiabilityExcel(TaxLiabilitySummary report) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle textStyle = createTextStyle(workbook);

            // --- Sheet 1: Tax Summary ---
            Sheet summarySheet = workbook.createSheet("Tax Summary");
            int rowIdx = 0;

            Row titleRow = summarySheet.createRow(rowIdx++);
            titleRow.createCell(0).setCellValue("Output Tax Liability Summary");
            titleRow.getCell(0).setCellStyle(createTitleStyle(workbook));
            summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            Row periodRow = summarySheet.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue("Period: " + report.getPeriodDescription());
            rowIdx++;

            // Summary data
            String[][] summaryData = {
                    {"Total Taxable Value", formatAmount(report.getTotalTaxableValue())},
                    {"Total CGST", formatAmount(report.getTotalCgst())},
                    {"Total SGST", formatAmount(report.getTotalSgst())},
                    {"Total IGST", formatAmount(report.getTotalIgst())},
                    {"Total Output Tax", formatAmount(report.getTotalOutputTax())},
                    {"", ""},
                    {"B2B Invoices", String.valueOf(report.getTotalB2BInvoices())},
                    {"B2B Taxable Value", formatAmount(report.getB2bTaxableValue())},
                    {"B2B Tax", formatAmount(report.getB2bTax())},
                    {"", ""},
                    {"B2C Invoices", String.valueOf(report.getTotalB2CInvoices())},
                    {"B2C Taxable Value", formatAmount(report.getB2cTaxableValue())},
                    {"B2C Tax", formatAmount(report.getB2cTax())}
            };

            for (String[] data : summaryData) {
                Row row = summarySheet.createRow(rowIdx++);
                Cell labelCell = row.createCell(0);
                labelCell.setCellValue(data[0]);
                if (!data[0].isEmpty()) {
                    labelCell.setCellStyle(headerStyle);
                }
                row.createCell(1).setCellValue(data[1]);
            }

            summarySheet.autoSizeColumn(0);
            summarySheet.setColumnWidth(1, 6000);

            // --- Sheet 2: Monthly Breakdown ---
            if (report.getMonthlyBreakdown() != null && !report.getMonthlyBreakdown().isEmpty()) {
                Sheet monthlySheet = workbook.createSheet("Monthly Breakdown");
                rowIdx = 0;

                Row mTitleRow = monthlySheet.createRow(rowIdx++);
                mTitleRow.createCell(0).setCellValue("Monthly Tax Breakdown");
                mTitleRow.getCell(0).setCellStyle(createTitleStyle(workbook));
                monthlySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
                rowIdx++;

                String[] mHeaders = {
                        "Month", "Invoice Count", "Taxable Value",
                        "CGST", "SGST", "IGST", "Total Tax"
                };

                Row mHeaderRow = monthlySheet.createRow(rowIdx++);
                for (int i = 0; i < mHeaders.length; i++) {
                    Cell cell = mHeaderRow.createCell(i);
                    cell.setCellValue(mHeaders[i]);
                    cell.setCellStyle(headerStyle);
                }

                for (MonthlyTaxBreakdown monthly : report.getMonthlyBreakdown()) {
                    Row row = monthlySheet.createRow(rowIdx++);
                    int col = 0;

                    createTextCell(row, col++, monthly.getMonth(), textStyle);
                    row.createCell(col++).setCellValue(monthly.getInvoiceCount());
                    createCurrencyCell(row, col++, monthly.getTaxableValue(), currencyStyle);
                    createCurrencyCell(row, col++, monthly.getCgst(), currencyStyle);
                    createCurrencyCell(row, col++, monthly.getSgst(), currencyStyle);
                    createCurrencyCell(row, col++, monthly.getIgst(), currencyStyle);
                    createCurrencyCell(row, col++, monthly.getTotalTax(), currencyStyle);
                }

                for (int i = 0; i < mHeaders.length; i++) {
                    monthlySheet.autoSizeColumn(i);
                }
                monthlySheet.createFreezePane(0, 3);
            }

            return toByteArray(workbook);

        } catch (Exception e) {
            log.error("Failed to generate Tax Liability Excel", e);
            throw new BusinessException("Failed to generate Tax Liability Excel: " + e.getMessage());
        }
    }

    // ================================================================
    // CELL CREATION HELPERS
    // ================================================================

    private void createTextCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createCurrencyCell(Row row, int col, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value.doubleValue() : 0.0);
        cell.setCellStyle(style);
    }

    private void createDateCell(Row row, int col, LocalDate date, CellStyle style) {
        Cell cell = row.createCell(col);
        if (date != null) {
            cell.setCellValue(date.format(DATE_FMT));
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }

    // ================================================================
    // STYLE CREATION
    // ================================================================

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName(FONT_NAME);
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName(FONT_NAME);
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName(FONT_NAME);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName(FONT_NAME);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTextStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName(FONT_NAME);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName(FONT_NAME);
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.DOUBLE);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    // ================================================================
    // UTILITY
    // ================================================================

    private byte[] toByteArray(XSSFWorkbook workbook) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Failed to write Excel to byte array: " + e.getMessage());
        }
    }

    private String formatAmount(BigDecimal value) {
        if (value == null) return "0.00";
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}