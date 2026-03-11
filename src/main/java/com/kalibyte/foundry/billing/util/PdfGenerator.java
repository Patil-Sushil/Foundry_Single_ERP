package com.kalibyte.foundry.billing.util;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallan;
import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallanItem;
import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.billing.invoice.entity.InvoiceItem;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

@Component
public class PdfGenerator {

    private static final DeviceRgb THEME_BLUE = new DeviceRgb(17, 51, 102);

    //------------------------------------------------
    // DELIVERY CHALLAN PDF
    //------------------------------------------------

    public byte[] generateDeliveryChallanPdf(DeliveryChallan dc, List<DeliveryChallanItem> items) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);

            doc.setMargins(20,30,20,30);

            addCompanyHeader(doc);

            addDivider(doc);

            doc.add(new Paragraph("DELIVERY CHALLAN")
                    .setBold()
                    .setFontColor(THEME_BLUE)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            addDcInfoSection(doc, dc);

            addDcItemsTable(doc, items);

            doc.add(new Paragraph("\nMaterial delivered in good condition.")
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER));

            addDivider(doc);

            addSignature(doc);

            doc.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating DC PDF", e);
        }
    }

    //------------------------------------------------
    // INVOICE PDF
    //------------------------------------------------

    public byte[] generateInvoicePdf(Invoice invoice, List<InvoiceItem> items) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);

            doc.setMargins(20,30,20,30);

            addCompanyHeader(doc);

            addDivider(doc);

            doc.add(new Paragraph("INVOICE")
                    .setBold()
                    .setFontColor(THEME_BLUE)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            addInvoiceInfoSection(doc, invoice);

            addInvoiceItemsTable(doc, items);

            addInvoiceTotals(doc, invoice);

            addDivider(doc);

            addSignature(doc);

            doc.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating Invoice PDF", e);
        }
    }

    //------------------------------------------------
    // COMPANY HEADER
    //------------------------------------------------

    private void addCompanyHeader(Document doc){

        doc.add(new Paragraph("MITTAL PRECISION STEEL FOUNDRY")
                .setBold()
                .setFontSize(22)
                .setFontColor(THEME_BLUE)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph("Plot No: A-12 MIDC Industrial Area Kolhapur - 416234")
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph("GST No: 27AACM1234P125 | Contact: 0214-2654321")
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER));
    }

    //------------------------------------------------
    // DC INFO SECTION
    //------------------------------------------------

    private void addDcInfoSection(Document doc, DeliveryChallan dc){

        Table table = new Table(new float[]{1,1}).useAllAvailableWidth();

        Cell left = new Cell().setBorder(Border.NO_BORDER);
        left.add(new Paragraph("To").setBold());
        left.add(new Paragraph(dc.getCustomer().getName()));
        left.add(new Paragraph(dc.getCustomer().getAddress()));
        left.add(new Paragraph("GST: " + dc.getCustomer().getGstNumber()));
        left.add(new Paragraph("Mobile: " + dc.getCustomer().getPhone()));

        Cell right = new Cell().setBorder(Border.NO_BORDER);
        right.add(new Paragraph("DC No: " + dc.getDcNumber()));
        right.add(new Paragraph("Date: " + dc.getDispatchDate()));
        right.add(new Paragraph("Vehicle: " + dc.getVehicleNumber()));
        right.add(new Paragraph("Transporter: " + dc.getTransportName()));
        right.add(new Paragraph("Place of Supply: " + dc.getOrder().getPlaceOfSupply()));
        right.add(new Paragraph("PO Ref: " + dc.getOrder().getPoReference()));

        table.addCell(left);
        table.addCell(right);

        doc.add(table);
    }

    //------------------------------------------------
    // DC ITEMS TABLE
    //------------------------------------------------

    private void addDcItemsTable(Document doc, List<DeliveryChallanItem> items){

        float[] cols = {1,4,2,2,1};
        Table table = new Table(cols).useAllAvailableWidth();

        String[] headers = {"Sr","Description","Grade","Weight(Kg)","Qty"};

        for(String h : headers){
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setBold())
                    .setBackgroundColor(THEME_BLUE)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        int sr=1;
        BigDecimal totalWeight = BigDecimal.ZERO;
        int totalQty = 0;

        for(DeliveryChallanItem item: items){

            table.addCell(createBodyCell(String.valueOf(sr++),TextAlignment.CENTER));
            table.addCell(createBodyCell(item.getOrderItem().getProductName(),TextAlignment.LEFT));
            table.addCell(createBodyCell("FG 260",TextAlignment.CENTER));
            table.addCell(createBodyCell(formatWeight(item.getWeight()),TextAlignment.RIGHT));
            table.addCell(createBodyCell(String.valueOf(item.getQuantity()),TextAlignment.CENTER));

            totalWeight = totalWeight.add(item.getWeight());
            totalQty += item.getQuantity();
        }

        doc.add(table);

        Table summary = new Table(new float[]{1,1}).useAllAvailableWidth();

        summary.addCell(new Cell().add(new Paragraph("Total Quantity: "+totalQty).setBold())
                .setBorder(Border.NO_BORDER));

        summary.addCell(new Cell().add(new Paragraph("Total Weight: "+formatWeight(totalWeight)+" Kg").setBold())
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT));

        doc.add(summary);
    }

    //------------------------------------------------
    // INVOICE INFO
    //------------------------------------------------

    private void addInvoiceInfoSection(Document doc, Invoice invoice){

        Table table = new Table(new float[]{1,1}).useAllAvailableWidth();

        Cell left = new Cell().setBorder(Border.NO_BORDER);
        left.add(new Paragraph("To").setBold());
        left.add(new Paragraph(invoice.getCustomer().getName()));
        left.add(new Paragraph(invoice.getCustomer().getAddress()));
        left.add(new Paragraph("GST: "+invoice.getCustomer().getGstNumber()));

        Cell right = new Cell().setBorder(Border.NO_BORDER);
        right.add(new Paragraph("Invoice No: "+invoice.getInvoiceNumber()));
        right.add(new Paragraph("Date: "+invoice.getInvoiceDate()));
        right.add(new Paragraph("Vehicle: "+invoice.getVehicleNumber()));

        table.addCell(left);
        table.addCell(right);

        doc.add(table);
    }

    //------------------------------------------------
    // INVOICE ITEMS
    //------------------------------------------------

    private void addInvoiceItemsTable(Document doc, List<InvoiceItem> items){

        float[] cols = {1,4,2,2,2,1,2};
        Table table = new Table(cols).useAllAvailableWidth();

        String[] headers = {"Sr","Description","Grade","Weight","Rate","Qty","Amount"};

        for(String h:headers){
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setBold())
                    .setBackgroundColor(THEME_BLUE)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        int sr=1;

        for(InvoiceItem item: items){

            table.addCell(createBodyCell(String.valueOf(sr++),TextAlignment.CENTER));
            table.addCell(createBodyCell(item.getOrderItem().getProductName(),TextAlignment.LEFT));
            table.addCell(createBodyCell("SG 500/7",TextAlignment.CENTER));
            table.addCell(createBodyCell(formatWeight(item.getWeight()),TextAlignment.RIGHT));
            table.addCell(createBodyCell(formatCurrency(item.getRate()),TextAlignment.RIGHT));
            table.addCell(createBodyCell(String.valueOf(item.getQuantity()),TextAlignment.CENTER));
            table.addCell(createBodyCell(formatCurrency(item.getAmount()),TextAlignment.RIGHT));
        }

        doc.add(table);
    }

    //------------------------------------------------
    // INVOICE TOTALS
    //------------------------------------------------

    private void addInvoiceTotals(Document doc, Invoice invoice){

        Table table = new Table(new float[]{3,1}).useAllAvailableWidth();

        table.addCell(new Cell().add(new Paragraph("Total Amount")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(formatCurrency(invoice.getSubtotal())))
                .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

        table.addCell(new Cell().add(new Paragraph("CGST @9%")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(formatCurrency(invoice.getCgst())))
                .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

        table.addCell(new Cell().add(new Paragraph("SGST @9%")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(formatCurrency(invoice.getSgst())))
                .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

        table.addCell(new Cell().add(new Paragraph("Grand Total").setBold()).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(formatCurrency(invoice.getTotalAmount())).setBold())
                .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

        doc.add(table);
    }

    //------------------------------------------------
    // SIGNATURE
    //------------------------------------------------

    private void addSignature(Document doc){

        Table table = new Table(new float[]{1,1}).useAllAvailableWidth().setMarginTop(30);

        table.addCell(new Cell()
                .add(new Paragraph("Prepared By: __________"))
                .setBorder(Border.NO_BORDER));

        Cell sign = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);

        sign.add(new Paragraph("For Mittal Precision Steel Foundry").setBold());

        table.addCell(sign);

        doc.add(table);
    }

    //------------------------------------------------
    // HELPERS
    //------------------------------------------------

    private Cell createBodyCell(String text, TextAlignment align){
        return new Cell()
                .add(new Paragraph(text).setFontSize(10))
                .setTextAlignment(align)
                .setBorder(new SolidBorder(ColorConstants.GRAY,0.5f));
    }

    private void addDivider(Document doc){
        LineSeparator line = new LineSeparator(new SolidLine());
        doc.add(line);
    }

    private String formatWeight(BigDecimal weight){
        DecimalFormat df = new DecimalFormat("#,##,##0.00");
        return df.format(weight);
    }

    private String formatCurrency(BigDecimal amount){
        DecimalFormat df = new DecimalFormat("₹ #,##,##0.00");
        return df.format(amount);
    }
}