package com.kalibyte.foundry.billing.util;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.*;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.*;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;

import com.kalibyte.foundry.billing.entity.*;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.order.entity.OrderItem;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Component
public class PdfGenerator {

    //------------------------------------------------
    // DELIVERY CHALLAN PDF
    //------------------------------------------------

    public byte[] generateDeliveryChallanPdf(DeliveryChallan dc, List<DeliveryChallanItem> items) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);

            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new PageNumberHandler());

            Document document = new Document(pdf);

            Customer customer = dc.getCustomer();

            addLogo(document);
            addCompanyHeader(document);

            document.add(new Paragraph("DELIVERY CHALLAN")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            addSeparator(document);

            addCustomerSection(document, customer);

            document.add(new Paragraph(
                    "DC Number : " + dc.getDcNumber() +
                            "\nDispatch Date : " + dc.getDispatchDate() +
                            "\nVehicle : " + dc.getVehicleNumber()));

            addSeparator(document);

            BigDecimal subtotal = addItemsTable(document, items);

            addTotals(document, customer, subtotal);

            addSignature(document);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Delivery Challan PDF", e);
        }
    }

    //------------------------------------------------
    // INVOICE PDF
    //------------------------------------------------

    public byte[] generateInvoicePdf(Invoice invoice, List<InvoiceItem> items) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);

            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new PageNumberHandler());

            Document document = new Document(pdf);

            Customer customer = invoice.getCustomer();

            addLogo(document);
            addCompanyHeader(document);

            document.add(new Paragraph("TAX INVOICE")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            addSeparator(document);

            addCustomerSection(document, customer);

            document.add(new Paragraph(
                    "Invoice Number : " + invoice.getInvoiceNumber() +
                            "\nInvoice Date : " + invoice.getInvoiceDate() +
                            "\nVehicle : " + invoice.getVehicleNumber()));

            addSeparator(document);

            BigDecimal subtotal = addInvoiceItemsTable(document, items);

            addTotals(document, customer, subtotal);

            addSignature(document);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException("Failed to generate Invoice PDF", e);
        }
    }

    //------------------------------------------------
    // INVOICE ITEM TABLE
    //------------------------------------------------

    private BigDecimal addInvoiceItemsTable(Document document, List<InvoiceItem> items) {

        float[] widths = {1,4,2,2,2,2};

        Table table = new Table(UnitValue.createPercentArray(widths))
                .useAllAvailableWidth();

        addHeader(table,"Sr");
        addHeader(table,"Description");
        addHeader(table,"Qty");
        addHeader(table,"Weight");
        addHeader(table,"Rate/KG");
        addHeader(table,"Amount");

        int sr = 1;
        BigDecimal subtotal = BigDecimal.ZERO;

        for (InvoiceItem item : items) {

            OrderItem oi = item.getOrderItem();

            table.addCell(createCell(String.valueOf(sr++)));
            table.addCell(createCell(oi.getProductName()));
            table.addCell(createCell(item.getQuantity().toString()));
            table.addCell(createCell(item.getWeight()+" KG"));
            table.addCell(createCell(formatINR(item.getRate())));
            table.addCell(createCell(formatINR(item.getAmount())));

            subtotal = subtotal.add(item.getAmount());
        }

        document.add(table);

        return subtotal;
    }

    //------------------------------------------------
    // COMMON METHODS
    //------------------------------------------------

    private void addLogo(Document document) {

        try {

            Image logo = new Image(
                    ImageDataFactory.create(
                            new ClassPathResource("static/logo.png")
                                    .getInputStream()
                                    .readAllBytes()
                    )
            );

            logo.setWidth(120);
            logo.setHorizontalAlignment(HorizontalAlignment.CENTER);

            document.add(logo);

        } catch (Exception ignored) {}
    }

    private void addCompanyHeader(Document document) {

        document.add(new Paragraph("KALI-BYTE PRECISION STEEL FOUNDRY")
                .setBold()
                .setFontSize(20)
                .setFontColor(ColorConstants.BLUE)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph(
                "Plot No A-12 MIDC Industrial Area Sangli - 416436")
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph(
                "GSTIN : 27AACM1234P125 | Phone : +91 9890649255 | Email : info@kalibytefoundry.com")
                .setTextAlignment(TextAlignment.CENTER));

        addSeparator(document);
    }

    private void addCustomerSection(Document document, Customer customer) {

        document.add(new Paragraph("Bill To").setBold());

        document.add(new Paragraph(customer.getName()));
        document.add(new Paragraph(customer.getAddress()));
        document.add(new Paragraph("GSTIN : " + customer.getGstNumber()));
        document.add(new Paragraph("Phone : " + customer.getPhone()));
    }

    private BigDecimal addItemsTable(Document document, List<DeliveryChallanItem> items) {

        float[] widths = {1,4,2,2,2,2};

        Table table = new Table(UnitValue.createPercentArray(widths))
                .useAllAvailableWidth();

        addHeader(table,"Sr");
        addHeader(table,"Description");
        addHeader(table,"Qty");
        addHeader(table,"Weight");
        addHeader(table,"Rate/KG");
        addHeader(table,"Amount");

        int sr = 1;
        BigDecimal subtotal = BigDecimal.ZERO;

        for (DeliveryChallanItem item : items) {

            OrderItem oi = item.getOrderItem();

            table.addCell(createCell(String.valueOf(sr++)));
            table.addCell(createCell(oi.getProductName()));
            table.addCell(createCell(item.getQuantity().toString()));
            table.addCell(createCell(item.getWeight()+" KG"));
            table.addCell(createCell(formatINR(item.getRate())));
            table.addCell(createCell(formatINR(item.getAmount())));

            subtotal = subtotal.add(item.getAmount());
        }

        document.add(table);

        return subtotal;
    }

    private void addTotals(Document document, Customer customer, BigDecimal subtotal) {

        boolean intraState = customer.getState()!=null &&
                customer.getState().equalsIgnoreCase("Maharashtra");

        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;

        if(intraState){

            cgst = subtotal.multiply(new BigDecimal("0.09"));
            sgst = subtotal.multiply(new BigDecimal("0.09"));

        }else{

            igst = subtotal.multiply(new BigDecimal("0.18"));
        }

        BigDecimal total = subtotal.add(cgst).add(sgst).add(igst);

        Table totals = new Table(UnitValue.createPercentArray(new float[]{60,40}))
                .setWidth(260)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT);

        totals.addCell(label("Subtotal"));
        totals.addCell(value(formatINR(subtotal)));

        totals.addCell(label("CGST (9%)"));
        totals.addCell(value(formatINR(cgst)));

        totals.addCell(label("SGST (9%)"));
        totals.addCell(value(formatINR(sgst)));

        totals.addCell(label("IGST (18%)"));
        totals.addCell(value(formatINR(igst)));

        totals.addCell(label("Grand Total"));
        totals.addCell(value(formatINR(total)));

        document.add(totals);

        addSeparator(document);
    }

    private void addSignature(Document document){

        Table sign = new Table(UnitValue.createPercentArray(new float[]{50,50}))
                .useAllAvailableWidth();

        sign.addCell(new Cell()
                .add(new Paragraph("For Kali-Byte Precision Steel Foundry"))
                .setBorder(Border.NO_BORDER));

        Cell right = new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);

        right.add(new Paragraph("Authorized Signatory"));

        sign.addCell(right);

        document.add(sign);
    }

    private void addHeader(Table table,String text){

        table.addHeaderCell(new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.BLUE)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private Cell createCell(String text){

        return new Cell()
                .add(new Paragraph(text))
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(new SolidBorder(1));
    }

    private Cell label(String text){

        return new Cell().add(new Paragraph(text).setBold());
    }

    private Cell value(String text){

        return new Cell()
                .add(new Paragraph(text))
                .setTextAlignment(TextAlignment.RIGHT);
    }

    private void addSeparator(Document document){

        LineSeparator line = new LineSeparator(new SolidLine());
        line.setMarginTop(10);
        line.setMarginBottom(10);

        document.add(line);
    }

    private String formatINR(BigDecimal amount){

        if(amount==null) return "₹ 0";

        NumberFormat formatter =
                NumberFormat.getCurrencyInstance(new Locale("en","IN"));

        return formatter.format(amount);
    }

    //------------------------------------------------
    // PAGE NUMBER HANDLER
    //------------------------------------------------

    class PageNumberHandler implements IEventHandler {

        public void handleEvent(Event event){

            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdf = docEvent.getDocument();
            PdfPage page = docEvent.getPage();

            int pageNumber = pdf.getPageNumber(page);

            Rectangle pageSize = page.getPageSize();

            PdfCanvas canvas = new PdfCanvas(page);

            try {

                canvas.beginText()
                        .setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(),10)
                        .moveText(pageSize.getWidth()/2-20,20)
                        .showText("Page "+pageNumber)
                        .endText();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}