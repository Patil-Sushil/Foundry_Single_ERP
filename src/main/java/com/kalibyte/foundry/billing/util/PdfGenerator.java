package com.kalibyte.foundry.billing.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.*;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.*;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.*;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.layout.*;
import com.itextpdf.layout.borders.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.kalibyte.foundry.billing.deliveryChallan.entity.*;
import com.kalibyte.foundry.billing.invoice.entity.*;
import com.kalibyte.foundry.order.entity.enums.GstType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Component
public class PdfGenerator {

    @Value("${app.company.name}")
    private String companyName;

    @Value("${app.company.address}")
    private String companyAddress;

    @Value("${app.company.gstNo}")
    private String companyGst;

    @Value("${app.company.contact}")
    private String companyContact;

    @Value("${app.company.email}")
    private String companyEmail;

    @Value("${app.company.logoPath}")
    private String logoPath;

    @Value("${app.company.bankName}")
    private String bankName;

    @Value("${app.company.branch}")
    private String bankBranch;

    @Value("${app.company.accountNo}")
    private String bankAccountNo;

    @Value("${app.company.ifsc}")
    private String bankIfsc;

    @Value("${app.company.termsPayment}")
    private String termsPayment;

    @Value("${app.company.termsDelivery}")
    private String termsDelivery;

    // ─── Theme Colors ───
    private static final DeviceRgb THEME_BLUE = new DeviceRgb(18, 53, 102);
    private static final DeviceRgb LIGHT_LINE = new DeviceRgb(200, 210, 225);
    private static final DeviceRgb ROW_BORDER = new DeviceRgb(160, 175, 195);
    private static final DeviceRgb LIGHT_BG = new DeviceRgb(245, 248, 252);

    // ─── Page Layout Constants ───
    private static final float PAGE_HEIGHT = PageSize.A4.getHeight();
    private static final float TOP_MARGIN = 18;
    private static final float BOTTOM_MARGIN = 18;
    private static final float LEFT_MARGIN = 28;
    private static final float RIGHT_MARGIN = 28;
    private static final float USABLE_HEIGHT = PAGE_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN;

    private static final float TABLE_ROW_HEIGHT = 30f;
    private static final float TABLE_HEADER_HEIGHT = 35f;

    private void addCompanyHeader(Document doc) {
        try {
            Image logo = new Image(com.itextpdf.io.image.ImageDataFactory.create(new ClassPathResource(logoPath).getInputStream().readAllBytes()));
            logo.setWidth(65);
            logo.setHorizontalAlignment(HorizontalAlignment.LEFT);
            doc.add(logo);
        } catch (Exception ignored) {}

        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{100}))
                .useAllAvailableWidth()
                .setMarginTop(-25);

        headerTable.addCell(new Cell().add(new Paragraph(companyName)
                        .setBold()
                        .setFontSize(14)
                        .setFontColor(THEME_BLUE))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER));

        headerTable.addCell(new Cell().add(new Paragraph(companyAddress)
                        .setFontSize(8))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER));

        headerTable.addCell(new Cell().add(new Paragraph("GST: " + companyGst + " | Contact: " + companyContact + " | Email: " + companyEmail)
                        .setFontSize(7))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2));

        doc.add(headerTable);
        doc.add(new LineSeparator(new SolidLine(1)).setMarginTop(1).setMarginBottom(2));
    }

    // ════════════════════════════════════════════════════════════
    //  DELIVERY CHALLAN PDF
    // ════════════════════════════════════════════════════════════
    public byte[] generateDeliveryChallanPdf(DeliveryChallan dc,
                                             List<DeliveryChallanItem> items) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(TOP_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN, LEFT_MARGIN);

            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new PageBorderHandler());

            // ── ORIGINAL COPY ──
            addDcSection(doc, dc, items, "ORIGINAL COPY");

            // ── Calculate if cut copy fits on same page ──
            float cutCopyHeight = calculateDcCutCopyHeight(items);
            float currentY = estimateCurrentY(doc, pdf);

            if (currentY - cutCopyHeight < BOTTOM_MARGIN + 20) {
                doc.add(new AreaBreak());
            }

            // ── CUT LINE ──
            addCutHereLine(doc);

            // ── CUSTOMER COPY ──
            addDcSection(doc, dc, items, "CUSTOMER COPY");

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating DC PDF", e);
        }
    }

    private void addDcSection(Document doc, DeliveryChallan dc,
                              List<DeliveryChallanItem> items, String copyLabel) {

        doc.add(new Paragraph(copyLabel)
                .setFontSize(8)
                .setFontColor(THEME_BLUE)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(2));

        addCompactDcHeader(doc, dc);

        doc.add(new Paragraph("DELIVERY CHALLAN")
                .setBold()
                .setFontSize(14)
                .setFontColor(THEME_BLUE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(4)
                .setMarginBottom(4));

        addThinRule(doc);
        addCompactDcInfo(doc, dc);
        addThinRule(doc);
        addDcItemsTable(doc, items);
        addDcTotalsRow(doc, dc, items);
        addCompactDcSignature(doc);
    }

    private void addCompactDcHeader(Document doc, DeliveryChallan dc) {
        addCompanyHeader(doc);

        Table info = new Table(new float[]{1f}).useAllAvailableWidth();
        info.setMarginBottom(4);
        
        Cell right = noBorderCell();
        right.add(miniKv("DC No: ",
                safe(dc != null ? dc.getDcNumber() : null)));
        right.add(miniKv("Date: ",
                formatDate(dc != null ? dc.getDispatchDate() : null)));
        right.add(miniKv("Vehicle No: ",
                safe(dc != null ? dc.getVehicleNumber() : null)));
        right.add(miniKv("Transporter: ",
                safe(dc != null ? dc.getTransportName() : null)));
        right.setTextAlignment(TextAlignment.RIGHT);

        info.addCell(right);
        doc.add(info);
    }

    private void addCompactDcInfo(Document doc, DeliveryChallan dc) {
        Table table = new Table(new float[]{2.2f, 1.8f}).useAllAvailableWidth();
        table.setMarginTop(4).setMarginBottom(4);

        Cell left = noBorderCell();
        left.add(new Paragraph("To,")
                .setBold().setFontColor(THEME_BLUE)
                .setFontSize(9).setMarginBottom(2));

        String custName = safe(dc != null && dc.getCustomer() != null
                ? dc.getCustomer().getName() : null);
        String custAddr = safe(dc != null && dc.getCustomer() != null
                ? dc.getCustomer().getAddress() : null);
        String custGst = safe(dc != null && dc.getCustomer() != null
                ? dc.getCustomer().getGstNumber() : null);
        String custPhone = safe(dc != null && dc.getCustomer() != null
                ? dc.getCustomer().getPhone() : null);

        left.add(new Paragraph(custName)
                .setBold().setFontSize(9).setMarginBottom(1));
        left.add(miniLine(custAddr));
        left.add(miniKv("GST: ", custGst));
        left.add(miniKv("Mobile: ", custPhone));

        Cell right = noBorderCell();
        String pos = (dc != null && dc.getOrder() != null)
                ? safe(dc.getOrder().getPlaceOfSupply()) : "-";
        right.add(miniKv("Place of Supply: ", pos));

        table.addCell(left);
        table.addCell(right);
        doc.add(table);
    }

    private void addDcItemsTable(Document doc, List<DeliveryChallanItem> items) {
        float[] cols = {1.0f, 4.5f, 1.5f, 1.8f, 1.2f, 1.5f, 1.2f, 1.8f};
        String[] headers = {"Sr No", "Description", "Grade", "Weight (Kg)", "Qty",
                "Rate", "GST%", "Amount"};

        Table table = new Table(cols).useAllAvailableWidth();
        table.setMarginTop(4);

        for (String h : headers) {
            table.addHeaderCell(headerCell(h));
        }

        int sr = 1;
        if (items != null) {
            for (DeliveryChallanItem item : items) {
                boolean isEven = (sr % 2 == 0);
                table.addCell(bodyCell(String.valueOf(sr++),
                        TextAlignment.CENTER, isEven));
                table.addCell(bodyCell(
                        safe(item != null && item.getOrderItem() != null
                                ? item.getOrderItem().getPartName() : null),
                        TextAlignment.LEFT, isEven));
                table.addCell(bodyCell("FG 260",
                        TextAlignment.CENTER, isEven));
                table.addCell(bodyCell(
                        formatWeight(item != null ? item.getWeight() : null),
                        TextAlignment.RIGHT, isEven));
                table.addCell(bodyCell(
                        String.valueOf(item != null ? item.getQuantity() : 0),
                        TextAlignment.CENTER, isEven));
                table.addCell(bodyCell(
                        formatCurrency(item != null ? item.getRate() : null),
                        TextAlignment.RIGHT, isEven));
                table.addCell(bodyCell(
                        (item != null && item.getGstPercentage() != null
                                ? item.getGstPercentage().stripTrailingZeros().toPlainString() + "%"
                                : "18%"),
                        TextAlignment.CENTER, isEven));
                table.addCell(bodyCell(
                        formatCurrency(item != null ? item.getAmount() : null),
                        TextAlignment.RIGHT, isEven));
            }
        }

        table.setSkipFirstHeader(false);
        table.setSkipLastFooter(false);
        doc.add(table);
    }

    private void addDcTotalsRow(Document doc, DeliveryChallan dc,
                                List<DeliveryChallanItem> items) {
        int totalQty = 0;
        BigDecimal totalWeight = BigDecimal.ZERO;

        if (items != null) {
            for (DeliveryChallanItem it : items) {
                if (it != null) {
                    totalQty += it.getQuantity();
                    if (it.getWeight() != null)
                        totalWeight = totalWeight.add(it.getWeight());
                }
            }
        }

        Table totals = new Table(new float[]{1f, 1f}).useAllAvailableWidth();
        totals.setMarginTop(2).setMarginBottom(4);
        totals.setBackgroundColor(LIGHT_BG);

        Cell l = noBorderCell();
        l.setPadding(6);
        l.add(new Paragraph("Total Quantity: " + totalQty)
                .setBold().setFontSize(9).setFontColor(THEME_BLUE));
        l.add(new Paragraph("Total Weight: " + formatWeight(totalWeight) + " Kg")
                .setBold().setFontSize(9).setFontColor(THEME_BLUE));

        Cell r = noBorderCell();
        r.setPadding(6);
        r.setTextAlignment(TextAlignment.RIGHT);

        if (dc != null) {
            r.add(new Paragraph("Subtotal: " + formatCurrency(dc.getSubtotal()))
                    .setFontSize(9).setFontColor(THEME_BLUE));

            if (dc.getGstType() == GstType.CGST_SGST) {
                BigDecimal halfPct = dc.getGstPercentage() != null
                        ? dc.getGstPercentage().divide(BigDecimal.valueOf(2)) : BigDecimal.valueOf(9);
                r.add(new Paragraph("CGST @ " + halfPct.stripTrailingZeros().toPlainString()
                        + "%: " + formatCurrency(dc.getCgst()))
                        .setFontSize(8).setFontColor(THEME_BLUE));
                r.add(new Paragraph("SGST @ " + halfPct.stripTrailingZeros().toPlainString()
                        + "%: " + formatCurrency(dc.getSgst()))
                        .setFontSize(8).setFontColor(THEME_BLUE));
            } else {
                r.add(new Paragraph("IGST @ "
                        + (dc.getGstPercentage() != null
                        ? dc.getGstPercentage().stripTrailingZeros().toPlainString() : "18")
                        + "%: " + formatCurrency(dc.getIgst()))
                        .setFontSize(8).setFontColor(THEME_BLUE));
            }

            r.add(new Paragraph("Total Amount: " + formatCurrency(dc.getTotalAmount()))
                    .setBold().setFontSize(10).setFontColor(THEME_BLUE));
        }

        totals.addCell(l);
        totals.addCell(r);
        doc.add(totals);
    }

    private void addCompactDcSignature(Document doc) {
        Table t = new Table(new float[]{1f, 1f}).useAllAvailableWidth();
        t.setMarginTop(6).setMarginBottom(4);

        Cell left = noBorderCell();
        left.add(new Paragraph("Received By: ________________")
                .setFontSize(8).setMarginTop(4));

        Cell right = noBorderCell();
        right.setTextAlignment(TextAlignment.RIGHT);
        right.add(new Paragraph("For " + companyName)
                .setBold().setFontSize(8)
                .setFontColor(THEME_BLUE).setMarginBottom(14));
        right.add(new Paragraph("Authorized Signatory")
                .setFontSize(7).setMarginTop(4));

        t.addCell(left);
        t.addCell(right);
        doc.add(t);
    }

    private void addCutHereLine(Document doc) {
        doc.add(new Paragraph("\n").setFontSize(2));

        DashedLine dashedLine = new DashedLine(0.8f);
        dashedLine.setColor(THEME_BLUE);
        LineSeparator sep = new LineSeparator(dashedLine);
        sep.setMarginTop(4);
        sep.setMarginBottom(4);
        doc.add(sep);

        doc.add(new Paragraph(
                "- - - - - - - - - - - - - - CUT HERE - - - - - - - - - - - - - -")
                .setFontSize(7)
                .setFontColor(LIGHT_LINE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

        LineSeparator sep2 = new LineSeparator(new DashedLine(0.8f));
        sep2.setMarginTop(4);
        sep2.setMarginBottom(4);
        doc.add(sep2);

        doc.add(new Paragraph("\n").setFontSize(2));
    }

    // ════════════════════════════════════════════════════════════
    //  INVOICE PDF
    // ════════════════════════════════════════════════════════════
    public byte[] generateInvoicePdf(Invoice invoice, List<InvoiceItem> items) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(15, RIGHT_MARGIN, 15, LEFT_MARGIN);

            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new PageBorderHandler());

            // ── Company Header ──
            addInvoiceCompanyHeader(doc, invoice);
            addThinRule(doc);

            // ── Title ──
            doc.add(new Paragraph("TAX INVOICE")
                    .setBold()
                    .setFontSize(15)
                    .setFontColor(THEME_BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setUnderline()
                    .setMarginTop(4)
                    .setMarginBottom(4));

            addThinRule(doc);

            // ── To Block ──
            addInvoiceToBlock(doc, invoice);
            addThinRule(doc);


            // ── Items Table ──
            addInvoiceItemsTable(doc, items);

            // ── Totals Block ──
            addInvoiceTotalsBlock(doc, invoice);
            addThinRule(doc);

            // ── Bank + Terms ──
            addBankAndTermsBlock(doc);
            addThinRule(doc);

            // ── Signature ──
            addInvoiceSignature(doc);
            addThinRule(doc);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating Invoice PDF", e);
        }
    }

    private void addInvoiceCompanyHeader(Document doc, Invoice invoice) {
        addCompanyHeader(doc);

        Table header = new Table(new float[]{1f}).useAllAvailableWidth();
        header.setMarginBottom(2);

        Cell right = noBorderCell();
        right.add(miniKv("Invoice No: ",
                safe(invoice != null ? invoice.getInvoiceNumber() : null)));
        right.add(miniKv("Date: ",
                formatDate(invoice != null ? invoice.getInvoiceDate() : null)));
        right.setTextAlignment(TextAlignment.RIGHT);

        header.addCell(right);
        doc.add(header);
    }

    private void addInvoiceToBlock(Document doc, Invoice invoice) {
        Table t = new Table(new float[]{1f}).useAllAvailableWidth();
        t.setMarginTop(4).setMarginBottom(4);

        Cell c = noBorderCell();
        c.add(new Paragraph("To,")
                .setBold().setFontColor(THEME_BLUE)
                .setFontSize(9).setMarginBottom(2));

        String name = safe(invoice != null && invoice.getCustomer() != null
                ? invoice.getCustomer().getName() : null);
        String addr = safe(invoice != null && invoice.getCustomer() != null
                ? invoice.getCustomer().getAddress() : null);
        String gst = safe(invoice != null && invoice.getCustomer() != null
                ? invoice.getCustomer().getGstNumber() : null);
        String phone = safe(invoice != null && invoice.getCustomer() != null
                ? invoice.getCustomer().getPhone() : null);

        c.add(new Paragraph(name)
                .setBold().setFontSize(9).setMarginBottom(1));
        c.add(miniLine(addr));
        c.add(miniKv("GST No: ", gst));
        c.add(miniKv("Mobile: ", phone));

        t.addCell(c);
        doc.add(t);
    }

    private void addInvoiceItemsTable(Document doc, List<InvoiceItem> items) {
        float[] cols = {0.8f, 3.5f, 1.3f, 1.4f, 1.3f, 0.8f, 1.0f, 1.2f, 1.8f};
        String[] headers = {"Sr", "Description", "Grade",
                "Weight(Kg)", "Rate", "Qty", "GST%", "GST Amt", "Amount"};

        Table table = new Table(cols).useAllAvailableWidth();
        table.setMarginTop(4);

        for (String h : headers) {
            table.addHeaderCell(headerCell(h));
        }

        // Footer row (totals)
        if (items != null && !items.isEmpty()) {
            BigDecimal totalAmt = BigDecimal.ZERO;
            BigDecimal totalGstAmt = BigDecimal.ZERO;
            for (InvoiceItem item : items) {
                if (item != null) {
                    if (item.getAmount() != null)
                        totalAmt = totalAmt.add(item.getAmount());
                    if (item.getGstAmount() != null)
                        totalGstAmt = totalGstAmt.add(item.getGstAmount());
                }
            }

            table.addFooterCell(footerCell("", TextAlignment.CENTER));
            table.addFooterCell(footerCell("", TextAlignment.LEFT));
            table.addFooterCell(footerCell("", TextAlignment.CENTER));
            table.addFooterCell(footerCell("", TextAlignment.RIGHT));
            table.addFooterCell(footerCell("", TextAlignment.RIGHT));
            table.addFooterCell(footerCell("Total", TextAlignment.CENTER));
            table.addFooterCell(footerCell("", TextAlignment.CENTER));
            table.addFooterCell(footerCell(
                    formatCurrency(totalGstAmt), TextAlignment.RIGHT));
            table.addFooterCell(footerCell(
                    formatCurrency(totalAmt), TextAlignment.RIGHT));
        }

        // Body rows
        int sr = 1;
        if (items != null) {
            for (InvoiceItem item : items) {
                boolean isEven = (sr % 2 == 0);

                table.addCell(bodyCell(String.valueOf(sr++),
                        TextAlignment.CENTER, isEven));
                table.addCell(bodyCell(
                        safe(item != null && item.getOrderItem() != null
                                ? item.getOrderItem().getPartName() : null),
                        TextAlignment.LEFT, isEven));
                table.addCell(bodyCell("SG 500/7",
                        TextAlignment.CENTER, isEven));
                table.addCell(bodyCell(
                        formatWeight(item != null ? item.getWeight() : null),
                        TextAlignment.RIGHT, isEven));
                table.addCell(bodyCell(
                        formatCurrency(item != null ? item.getRate() : null),
                        TextAlignment.RIGHT, isEven));
                table.addCell(bodyCell(
                        String.valueOf(item != null ? item.getQuantity() : 0),
                        TextAlignment.CENTER, isEven));
                table.addCell(bodyCell(
                        (item != null && item.getGstPercentage() != null
                                ? item.getGstPercentage().stripTrailingZeros().toPlainString() + "%"
                                : "18%"),
                        TextAlignment.CENTER, isEven));
                table.addCell(bodyCell(
                        formatCurrency(item != null ? item.getGstAmount() : null),
                        TextAlignment.RIGHT, isEven));
                table.addCell(bodyCell(
                        formatCurrency(item != null ? item.getAmount() : null),
                        TextAlignment.RIGHT, isEven));
            }
        }

        table.setSkipFirstHeader(false);
        table.setSkipLastFooter(false);
        doc.add(table);
    }

    private void addInvoiceTotalsBlock(Document doc, Invoice invoice) {
        Table t = new Table(new float[]{2.5f, 1f}).useAllAvailableWidth();
        t.setMarginTop(4);

        t.addCell(totalLabel("Subtotal"));
        t.addCell(totalValue(formatCurrency(
                invoice != null ? invoice.getSubtotal() : null)));

        // Dynamic GST display based on type
        if (invoice != null && invoice.getGstType() == GstType.CGST_SGST) {
            BigDecimal halfPct = invoice.getGstPercentage() != null
                    ? invoice.getGstPercentage().divide(BigDecimal.valueOf(2))
                    : BigDecimal.valueOf(9);

            t.addCell(totalLabel("CGST @ " + halfPct.stripTrailingZeros().toPlainString() + "%"));
            t.addCell(totalValue(formatCurrency(
                    invoice != null ? invoice.getCgst() : null)));

            t.addCell(totalLabel("SGST @ " + halfPct.stripTrailingZeros().toPlainString() + "%"));
            t.addCell(totalValue(formatCurrency(
                    invoice != null ? invoice.getSgst() : null)));
        } else {
            String igstPct = (invoice != null && invoice.getGstPercentage() != null)
                    ? invoice.getGstPercentage().stripTrailingZeros().toPlainString()
                    : "18";
            t.addCell(totalLabel("IGST @ " + igstPct + "%"));
            t.addCell(totalValue(formatCurrency(
                    invoice != null ? invoice.getIgst() : null)));
        }

        // Total GST row
        t.addCell(totalLabel("Total GST"));
        t.addCell(totalValue(formatCurrency(
                invoice != null ? invoice.getTotalGst() : null)));

        // Grand Total highlighted bar
        Cell grandL = noBorderCell();
        grandL.setBackgroundColor(THEME_BLUE);
        grandL.setPadding(6);
        grandL.add(new Paragraph("GRAND TOTAL")
                .setBold().setFontSize(11)
                .setFontColor(ColorConstants.WHITE));

        Cell grandR = noBorderCell();
        grandR.setBackgroundColor(THEME_BLUE);
        grandR.setPadding(6);
        grandR.setTextAlignment(TextAlignment.RIGHT);
        grandR.add(new Paragraph(formatCurrency(
                invoice != null ? invoice.getTotalAmount() : null))
                .setBold().setFontSize(11)
                .setFontColor(ColorConstants.WHITE));

        t.addCell(grandL);
        t.addCell(grandR);
        doc.add(t);

        // Amount in words
        if (invoice != null && invoice.getTotalAmount() != null) {
            doc.add(new Paragraph()
                    .add(kvInline("Amount in Words: ",
                            convertToWords(invoice.getTotalAmount())))
                    .setFontSize(8)
                    .setMarginTop(2)
                    .setMarginBottom(2));
        }
    }

    private void addBankAndTermsBlock(Document doc) {
        Table outer = new Table(new float[]{1f, 1f}).useAllAvailableWidth();
        outer.setMarginTop(4).setMarginBottom(4);

        Cell bank = noBorderCell();
        bank.add(new Paragraph("Bank Details:")
                .setBold().setFontColor(THEME_BLUE)
                .setFontSize(9).setMarginBottom(2));
        bank.add(miniKv("Account Name: ", companyName));
        bank.add(miniKv("Bank: ", bankName));
        bank.add(miniKv("Branch: ", bankBranch));
        bank.add(miniKv("Account No: ", bankAccountNo));
        bank.add(miniKv("IFSC: ", bankIfsc));

        Cell terms = noBorderCell();
        terms.add(new Paragraph("Terms & Conditions:")
                .setBold().setFontColor(THEME_BLUE)
                .setFontSize(9).setMarginBottom(2));
        terms.add(miniKv("Payment: ", termsPayment));
        terms.add(miniKv("Delivery: ", termsDelivery));
        terms.add(new Paragraph("* Goods once sold will not be taken back.")
                .setFontSize(7).setMarginTop(2));
        terms.add(new Paragraph("* Subject to Kolhapur jurisdiction.")
                .setFontSize(7));

        outer.addCell(bank);
        outer.addCell(terms);
        doc.add(outer);
    }

    private void addInvoiceSignature(Document doc) {
        Table t = new Table(new float[]{1f, 1f}).useAllAvailableWidth();
        t.setMarginTop(4).setMarginBottom(4);

        Cell left = noBorderCell();
        left.add(new Paragraph("Customer Seal & Signature")
                .setFontSize(8).setFontColor(THEME_BLUE).setMarginTop(18));

        Cell right = noBorderCell();
        right.setTextAlignment(TextAlignment.RIGHT);
        right.add(new Paragraph("For " + companyName)
                .setBold().setFontSize(9)
                .setFontColor(THEME_BLUE).setMarginBottom(12));
        right.add(new Paragraph("______________________________")
                .setFontSize(8));
        right.add(new Paragraph("Authorized Signatory")
                .setFontSize(7).setMarginTop(2));

        t.addCell(left);
        t.addCell(right);
        doc.add(t);
    }

    // ════════════════════════════════════════════════════════════
    //  CELL STYLES
    // ════════════════════════════════════════════════════════════

    private Cell headerCell(String text) {
        return new Cell()
                .setBackgroundColor(THEME_BLUE)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setPadding(4)
                .setBorder(new SolidBorder(ColorConstants.WHITE, 0.5f))
                .add(new Paragraph(text).setFontSize(8));
    }

    private Cell bodyCell(String text, TextAlignment align, boolean shaded) {
        Cell cell = new Cell()
                .setTextAlignment(align)
                .setPadding(3)
                .setBorder(new SolidBorder(ROW_BORDER, 0.5f))
                .add(new Paragraph(safe(text)).setFontSize(8));
        if (shaded) {
            cell.setBackgroundColor(LIGHT_BG);
        }
        return cell;
    }

    private Cell footerCell(String text, TextAlignment align) {
        return new Cell()
                .setBackgroundColor(LIGHT_BG)
                .setTextAlignment(align)
                .setBold()
                .setPadding(4)
                .setBorder(new SolidBorder(THEME_BLUE, 0.8f))
                .add(new Paragraph(text).setFontSize(8)
                        .setFontColor(THEME_BLUE));
    }

    private Cell totalLabel(String text) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_LINE, 0.5f))
                .setPadding(4)
                .add(new Paragraph(text)
                        .setFontColor(THEME_BLUE).setBold().setFontSize(9));
    }

    private Cell totalValue(String text) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_LINE, 0.5f))
                .setPadding(4)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(text).setBold().setFontSize(9));
    }

    private Cell noBorderCell() {
        return new Cell().setBorder(Border.NO_BORDER);
    }

    // ════════════════════════════════════════════════════════════
    //  HELPER PARAGRAPHS
    // ════════════════════════════════════════════════════════════

    private Paragraph miniLine(String text) {
        return new Paragraph(safe(text))
                .setFontSize(8).setMargin(0).setMarginBottom(1);
    }

    private Paragraph miniKv(String key, String value) {
        Paragraph p = new Paragraph()
                .setFontSize(8).setMargin(0).setMarginBottom(1);
        p.add(new Text(key).setBold().setFontColor(THEME_BLUE));
        p.add(new Text(safe(value)));
        return p;
    }

    private Paragraph kvInline(String key, String value) {
        Paragraph p = new Paragraph().setMargin(0);
        p.add(new Text(key).setBold().setFontColor(THEME_BLUE));
        p.add(new Text(safe(value)));
        return p;
    }

    // ════════════════════════════════════════════════════════════
    //  RULES / DIVIDERS
    // ════════════════════════════════════════════════════════════

    private void addThinRule(Document doc) {
        SolidLine solid = new SolidLine(0.5f);
        solid.setColor(LIGHT_LINE);
        LineSeparator line = new LineSeparator(solid);
        line.setMarginTop(1);
        line.setMarginBottom(1);
        doc.add(line);
    }

    // ════════════════════════════════════════════════════════════
    //  LAYOUT CALCULATION
    // ════════════════════════════════════════════════════════════

    private float estimateCurrentY(Document doc, PdfDocument pdf) {
        return USABLE_HEIGHT / 2;
    }

    private float calculateDcCutCopyHeight(List<DeliveryChallanItem> items) {
        float height = 0;
        height += 20;
        height += 80;
        height += 30;
        height += 80;
        height += TABLE_HEADER_HEIGHT;
        height += (items != null ? items.size() : 0) * TABLE_ROW_HEIGHT;
        height += 80;   // totals (now larger with GST)
        height += 80;
        height += 40;
        return height;
    }

    // ════════════════════════════════════════════════════════════
    //  DATA FORMATTING
    // ════════════════════════════════════════════════════════════

    private String formatWeight(BigDecimal weight) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(weight != null ? weight : BigDecimal.ZERO);
    }

    private String formatCurrency(BigDecimal amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return "Rs. " + df.format(amount != null ? amount : BigDecimal.ZERO);
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    private String formatDate(Object date) {
        if (date == null) return "-";
        try {
            if (date instanceof LocalDate ld) {
                return ld.format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            if (date instanceof Date d) {
                return new java.text.SimpleDateFormat("dd/MM/yyyy")
                        .format(d);
            }
        } catch (Exception ignored) {
        }
        return String.valueOf(date);
    }

    private String convertToWords(BigDecimal amount) {
        if (amount == null) return "-";

        long rupees = amount.longValue();
        int paise = amount.subtract(BigDecimal.valueOf(rupees))
                .multiply(BigDecimal.valueOf(100)).intValue();

        String[] ones = {"", "One", "Two", "Three", "Four", "Five",
                "Six", "Seven", "Eight", "Nine", "Ten",
                "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen",
                "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty",
                "Sixty", "Seventy", "Eighty", "Ninety"};

        if (rupees == 0) return "Zero Rupees Only";

        StringBuilder sb = new StringBuilder();
        sb.append(numberToWords(rupees, ones, tens));
        sb.append(" Rupees");

        if (paise > 0) {
            sb.append(" and ");
            sb.append(numberToWords(paise, ones, tens));
            sb.append(" Paise");
        }

        sb.append(" Only");
        return sb.toString().trim().replaceAll("\\s+", " ");
    }

    private String numberToWords(long num, String[] ones, String[] tens) {
        if (num == 0) return "";
        if (num < 20) return ones[(int) num];
        if (num < 100)
            return tens[(int) (num / 10)] + " " + ones[(int) (num % 10)];
        if (num < 1000)
            return ones[(int) (num / 100)] + " Hundred "
                    + numberToWords(num % 100, ones, tens);
        if (num < 100000)
            return numberToWords(num / 1000, ones, tens) + " Thousand "
                    + numberToWords(num % 1000, ones, tens);
        if (num < 10000000)
            return numberToWords(num / 100000, ones, tens) + " Lakh "
                    + numberToWords(num % 100000, ones, tens);
        return numberToWords(num / 10000000, ones, tens) + " Crore "
                + numberToWords(num % 10000000, ones, tens);
    }

    // ════════════════════════════════════════════════════════════
    //  PAGE BORDER + PAGE NUMBER HANDLER
    // ════════════════════════════════════════════════════════════

    private static class PageBorderHandler implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            Rectangle pageSize = page.getPageSize();

            PdfCanvas canvas = new PdfCanvas(
                    page.newContentStreamBefore(),
                    page.getResources(), pdfDoc);

            canvas.setStrokeColor(LIGHT_LINE)
                    .setLineWidth(0.8f)
                    .rectangle(
                            pageSize.getLeft() + 14,
                            pageSize.getBottom() + 10,
                            pageSize.getWidth() - 28,
                            pageSize.getHeight() - 20)
                    .stroke();

            try {
                PdfFont font = PdfFontFactory.createFont(
                        StandardFonts.HELVETICA);
                int pageNum = pdfDoc.getPageNumber(page);
                int totalPages = pdfDoc.getNumberOfPages();

                canvas.beginText()
                        .setFontAndSize(font, 7)
                        .moveText(
                                pageSize.getWidth() / 2 - 20,
                                pageSize.getBottom() + 14)
                        .showText("Page " + pageNum + " of " + totalPages)
                        .endText();
            } catch (Exception e) {
                // Silently skip page number if font fails
            }

            canvas.release();
        }
    }
}