package com.kalibyte.foundry.inventory.purchaseinvoice.service;

import com.kalibyte.foundry.inventory.purchaseinvoice.entity.PurchaseInvoice;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PurchaseInvoiceExportService {

    private final PurchaseInvoiceService purchaseInvoiceService;
    private static final String COMPANY_STATE = "Maharashtra";

    public PurchaseInvoiceExportService(PurchaseInvoiceService purchaseInvoiceService) {
        this.purchaseInvoiceService = purchaseInvoiceService;
    }

    public byte[] exportGstReport(LocalDate startDate, LocalDate endDate) throws IOException {
        List<PurchaseInvoice> invoices = purchaseInvoiceService.getInvoicesForExport(startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Purchase Invoice GST Report");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                "Vendor Invoice No", "Vendor Invoice Date", "Vendor Name", "Vendor GSTIN",
                "PO Number", "Inward Number", "Invoice Amount", "Inward Amount",
                "Amount Mismatch", "Source", "Verified", "Remarks"
            };

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Highlight style for mismatches
            CellStyle mismatchStyle = workbook.createCellStyle();
            Font mismatchFont = workbook.createFont();
            mismatchFont.setColor(IndexedColors.RED.getIndex());
            mismatchFont.setBold(true);
            mismatchStyle.setFont(mismatchFont);

            int rowIdx = 1;
            for (PurchaseInvoice invoice : invoices) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(invoice.getVendorInvoiceNumber());
                row.createCell(1).setCellValue(invoice.getVendorInvoiceDate().toString());
                row.createCell(2).setCellValue(invoice.getVendor().getName());
                row.createCell(3).setCellValue(
                    invoice.getVendor().getGstNumber() != null ? invoice.getVendor().getGstNumber() : "");
                row.createCell(4).setCellValue(
                    invoice.getPurchaseOrder() != null ? invoice.getPurchaseOrder().getPoNumber() : "");
                row.createCell(5).setCellValue(
                    invoice.getMaterialInward() != null ? invoice.getMaterialInward().getInwardNumber() : "");
                row.createCell(6).setCellValue(
                    invoice.getInvoiceAmount() != null ? invoice.getInvoiceAmount().doubleValue() : 0);
                row.createCell(7).setCellValue(
                    invoice.getMaterialInward() != null && invoice.getMaterialInward().getTotalAmount() != null
                        ? invoice.getMaterialInward().getTotalAmount().doubleValue() : 0);

                // Amount mismatch with red highlight
                Cell mismatchCell = row.createCell(8);
                BigDecimal mismatch = invoice.getAmountMismatch();
                if (mismatch != null) {
                    mismatchCell.setCellValue(mismatch.doubleValue());
                    if (invoice.hasAmountMismatch()) {
                        mismatchCell.setCellStyle(mismatchStyle);
                    }
                } else {
                    mismatchCell.setCellValue("N/A");
                }

                row.createCell(9).setCellValue(invoice.getSource());
                row.createCell(10).setCellValue(invoice.getIsVerified() ? "Yes" : "No");
                row.createCell(11).setCellValue(invoice.getRemarks() != null ? invoice.getRemarks() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
