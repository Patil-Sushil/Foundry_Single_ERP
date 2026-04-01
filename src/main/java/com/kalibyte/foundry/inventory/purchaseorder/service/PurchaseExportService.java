package com.kalibyte.foundry.inventory.purchaseorder.service;

import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrderItem;
import com.kalibyte.foundry.inventory.purchaseorder.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseExportService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private static final String COMPANY_STATE = "Maharashtra";

    public byte[] exportPurchaseReport(LocalDate startDate, LocalDate endDate) throws IOException {
        List<PurchaseOrder> orders = purchaseOrderRepository.findByPoDateBetween(startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Purchase Report CA");

            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Date", "PO Number", "Vendor Name", "Vendor GSTIN", "HSN Code", 
                                "Taxable Value", "GST %", "CGST", "SGST", "IGST", "Total Amount"};
            
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (PurchaseOrder order : orders) {
                String vendorState = order.getVendor().getState();
                boolean isSameState = COMPANY_STATE.equalsIgnoreCase(vendorState != null ? vendorState.trim() : "");
                String vendorGstin = order.getVendor().getGstNumber();

                for (PurchaseOrderItem item : order.getOrderItems()) {
                    Row row = sheet.createRow(rowIdx++);

                    row.createCell(0).setCellValue(order.getPoDate().toString());
                    row.createCell(1).setCellValue(order.getPoNumber());
                    row.createCell(2).setCellValue(order.getVendor().getName());
                    row.createCell(3).setCellValue(vendorGstin != null ? vendorGstin : "");
                    row.createCell(4).setCellValue(item.getHsnCode() != null ? item.getHsnCode() : "");
                    
                    BigDecimal taxableValue = item.getTaxableValue();
                    BigDecimal gstRate = item.getGstRate() != null ? item.getGstRate() : BigDecimal.ZERO;
                    BigDecimal taxAmount = item.getTaxAmount() != null ? item.getTaxAmount() : BigDecimal.ZERO;

                    row.createCell(5).setCellValue(taxableValue.doubleValue());
                    row.createCell(6).setCellValue(gstRate.doubleValue());

                    if (isSameState) {
                        BigDecimal halfTax = taxAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                        row.createCell(7).setCellValue(halfTax.doubleValue()); // CGST
                        row.createCell(8).setCellValue(halfTax.doubleValue()); // SGST
                        row.createCell(9).setCellValue(0); // IGST
                    } else {
                        row.createCell(7).setCellValue(0); // CGST
                        row.createCell(8).setCellValue(0); // SGST
                        row.createCell(9).setCellValue(taxAmount.doubleValue()); // IGST
                    }

                    row.createCell(10).setCellValue(item.getTotalValue() != null ? item.getTotalValue().doubleValue() : taxableValue.doubleValue());
                }
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
