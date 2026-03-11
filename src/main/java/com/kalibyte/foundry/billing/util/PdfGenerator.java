package com.kalibyte.foundry.billing.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.DashedLine;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallan;
import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallanItem;
import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.billing.invoice.entity.InvoiceItem;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * PDF Generator (iText 7)
 *
 * هدف: DC + Invoice same-to-same look as your provided images:
 * - Top company name + two-column header info (left company info, right document meta)
 * - Center title with divider lines
 * - To / Document info section
 * - Dark-blue table headers
 * - Totals row with left/right alignment
 * - DC: print items table AGAIN after totals (for cutting / customer receipt)
 * - Invoice: totals block + bank details + terms & conditions + signature block
 *
 * NOTE:
 * - Heat No. column is added in DC table. If you don't have heat no in your entity,
 *   it prints "-" by default (see resolveHeatNo()).
 * - Currency symbol "₹" needs a font that supports it. If your PDF shows a square box,
 *   replace formatCurrency() to use "Rs." OR embed a Unicode TTF font.
 */
@Component
public class PdfGenerator {

    // ─── Theme Colors ───
    private static final DeviceRgb THEME_BLUE = new DeviceRgb(18, 53, 102);
    private static final DeviceRgb LIGHT_LINE = new DeviceRgb(200, 210, 225);
    private static final DeviceRgb ROW_BORDER = new DeviceRgb(160, 175, 195);
    private static final DeviceRgb LIGHT_BG = new DeviceRgb(245, 248, 252);

    // ─── Company Constants ───
    private static final String COMPANY_NAME = "KALI-BYTE PRECISION STEEL FOUNDRY";
    private static final String COMPANY_PLOT = "Plot No: A-12, MIDC Industrial Area, Sangli - 416234";
    private static final String COMPANY_GST = "GST No: 27AACM1234P125";
    private static final String COMPANY_CONTACT = "Contact No: 0214-2654321";
    private static final String COMPANY_EMAIL = "Email: info@kalibytefoundry.com";

    // ─── Bank Details ───
    private static final String BANK_ACCOUNT_NAME = "Kalibyte Precision Steel Foundry";
    private static final String BANK_NAME = "HDFC Bank";
    private static final String BANK_BRANCH = "Vishrambag (Sangli)";
    private static final String BANK_ACCOUNT_NO = "5010012345678";
    private static final String BANK_IFSC = "HDFC0000123";

    // ─── Terms ───
    private static final String TERMS_PAYMENT = "50% Advance, 50% Before Dispatch";
    private static final String TERMS_DELIVERY = "Mumbai";

    // ─── Page Layout Constants ───
    private static final float PAGE_HEIGHT = PageSize.A4.getHeight();
    private static final float TOP_MARGIN = 18;
    private static final float BOTTOM_MARGIN = 18;
    private static final float LEFT_MARGIN = 28;
    private static final float RIGHT_MARGIN = 28;
    private static final float USABLE_HEIGHT = PAGE_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN;

    // Estimated heights for layout calculation
    private static final float TABLE_ROW_HEIGHT = 30f;
    private static final float TABLE_HEADER_HEIGHT = 35f;

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

            // Add page border handler
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new PageBorderHandler());

            // ── ORIGINAL COPY (top half or full pages) ──
            addDcSection(doc, dc, items, "ORIGINAL COPY");

            // ── Calculate if cut copy fits on same page ──
            float cutCopyHeight = calculateDcCutCopyHeight(items);
            float currentY = estimateCurrentY(doc, pdf);

            if (currentY - cutCopyHeight < BOTTOM_MARGIN + 20) {
                doc.add(new AreaBreak());
            }

            // ── CUT LINE ──
            addCutHereLine(doc);

            // ── CUSTOMER COPY (bottom half or new page) ──
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
        addDcTotalsRow(doc, items);
        addCompactDcSignature(doc);
    }

    private void addCompactDcHeader(Document doc, DeliveryChallan dc) {
        Table header = new Table(new float[]{2.2f, 1.8f}).useAllAvailableWidth();
        header.setMarginBottom(4);

        Cell left = noBorderCell();
        left.add(new Paragraph(COMPANY_NAME)
                .setBold().setFontSize(12)
                .setFontColor(THEME_BLUE).setMarginBottom(2));
        left.add(miniLine(COMPANY_PLOT));
        left.add(miniLine(COMPANY_GST));
        left.add(miniLine(COMPANY_CONTACT + " | " + COMPANY_EMAIL));

        Cell right = noBorderCell();
        right.add(miniKv("DC No: ",
                safe(dc != null ? dc.getDcNumber() : null)));
        right.add(miniKv("Date: ",
                formatDate(dc != null ? dc.getDispatchDate() : null)));
        right.add(miniKv("Vehicle No: ",
                safe(dc != null ? dc.getVehicleNumber() : null)));
        right.add(miniKv("Transporter: ",
                safe(dc != null ? dc.getTransportName() : null)));

        header.addCell(left);
        header.addCell(right);
        doc.add(header);
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
        float[] cols = {1.0f, 5.5f, 1.8f, 2.0f, 1.5f};
        String[] headers = {"Sr No", "Description", "Grade", "Weight (Kg)", "Qty"};

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
                                ? item.getOrderItem().getProductName() : null),
                        TextAlignment.LEFT, isEven));
                table.addCell(bodyCell("FG 260",
                        TextAlignment.CENTER, isEven));
                table.addCell(bodyCell(
                        formatWeight(item != null ? item.getWeight() : null),
                        TextAlignment.RIGHT, isEven));
                table.addCell(bodyCell(
                        String.valueOf(item != null ? item.getQuantity() : 0),
                        TextAlignment.CENTER, isEven));
            }
        }

        table.setSkipFirstHeader(false);
        table.setSkipLastFooter(false);
        doc.add(table);
    }

    private void addDcTotalsRow(Document doc, List<DeliveryChallanItem> items) {
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

        Cell r = noBorderCell();
        r.setPadding(6);
        r.setTextAlignment(TextAlignment.RIGHT);
        r.add(new Paragraph("Total Weight: " + formatWeight(totalWeight) + " Kg")
                .setBold().setFontSize(9).setFontColor(THEME_BLUE));

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
        right.add(new Paragraph("For " + COMPANY_NAME)
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
            doc.setMargins(TOP_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN, LEFT_MARGIN);

            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new PageBorderHandler());

            // ── Company Header ──
            addInvoiceCompanyHeader(doc, invoice);
            addThinRule(doc);

            // ── Title ──
            doc.add(new Paragraph("TAX INVOICE")
                    .setBold()
                    .setFontColor(THEME_BLUE)
                    .setFontSize(18)
                    .setFontColor(THEME_BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setUnderline()
                    .setMarginTop(6)
                    .setMarginBottom(6));

            addThinRule(doc);

            // ── To Block ──
            addInvoiceToBlock(doc, invoice);
            addThinRule(doc);

            // ── Subject ──
            doc.add(new Paragraph()
                    .add(kvInline("Subject: ", "Invoice for SG Iron Castings"))
                    .setMarginTop(6).setMarginBottom(6));

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
        doc.add(new Paragraph(COMPANY_NAME)
                .setBold().setFontSize(20).setFontColor(THEME_BLUE)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));

        Table header = new Table(new float[]{2.2f, 1.8f}).useAllAvailableWidth();
        header.setMarginBottom(4);

        Cell left = noBorderCell();
        left.add(miniLine(COMPANY_PLOT));
        left.add(miniLine(COMPANY_GST));
        left.add(miniLine(COMPANY_CONTACT + " | " + COMPANY_EMAIL));

        Cell right = noBorderCell();
        right.add(miniKv("Invoice No: ",
                safe(invoice != null ? invoice.getInvoiceNumber() : null)));
        right.add(miniKv("Date: ",
                formatDate(invoice != null ? invoice.getInvoiceDate() : null)));

        header.addCell(left);
        header.addCell(right);
        doc.add(header);
    }

    private void addInvoiceToBlock(Document doc, Invoice invoice) {
        Table t = new Table(new float[]{1f}).useAllAvailableWidth();
        t.setMarginTop(6).setMarginBottom(6);

        Cell c = noBorderCell();
        c.add(new Paragraph("To,")
                .setBold().setFontColor(THEME_BLUE)
                .setFontSize(10).setMarginBottom(3));

        String name = safe(invoice != null && invoice.getCustomer() != null
                ? invoice.getCustomer().getName() : null);
        String addr = safe(invoice != null && invoice.getCustomer() != null
                ? invoice.getCustomer().getAddress() : null);
        String gst = safe(invoice != null && invoice.getCustomer() != null
                ? invoice.getCustomer().getGstNumber() : null);
        String phone = safe(invoice != null && invoice.getCustomer() != null
                ? invoice.getCustomer().getPhone() : null);

        c.add(new Paragraph(name)
                .setBold().setFontSize(10).setMarginBottom(1));
        c.add(miniLine(addr));
        c.add(miniKv("GST No: ", gst));
        c.add(miniKv("Mobile: ", phone));

        t.addCell(c);
        doc.add(t);
    }

    private void addInvoiceItemsTable(Document doc, List<InvoiceItem> items) {
        float[] cols = {0.8f, 4.0f, 1.5f, 1.6f, 1.5f, 0.8f, 2.0f};
        String[] headers = {"Sr", "Description", "Grade",
                "Weight(Kg)", "Rate", "Qty", "Amount"};

        Table table = new Table(cols).useAllAvailableWidth();
        table.setMarginTop(6);

        // Header row - repeats on every page
        for (String h : headers) {
            table.addHeaderCell(headerCell(h));
        }

        // Footer row (totals) - appears on last page only
        if (items != null && !items.isEmpty()) {
            BigDecimal totalAmt = BigDecimal.ZERO;
            for (InvoiceItem item : items) {
                if (item != null && item.getAmount() != null) {
                    totalAmt = totalAmt.add(item.getAmount());
                }
            }

            table.addFooterCell(footerCell("", TextAlignment.CENTER));
            table.addFooterCell(footerCell("", TextAlignment.LEFT));
            table.addFooterCell(footerCell("", TextAlignment.CENTER));
            table.addFooterCell(footerCell("", TextAlignment.RIGHT));
            table.addFooterCell(footerCell("", TextAlignment.RIGHT));
            table.addFooterCell(footerCell("Total", TextAlignment.CENTER));
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
                                ? item.getOrderItem().getProductName() : null),
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
        t.setMarginTop(8);

        t.addCell(totalLabel("Subtotal"));
        t.addCell(totalValue(formatCurrency(
                invoice != null ? invoice.getSubtotal() : null)));

        t.addCell(totalLabel("CGST @ 9%"));
        t.addCell(totalValue(formatCurrency(
                invoice != null ? invoice.getCgst() : null)));

        t.addCell(totalLabel("SGST @ 9%"));
        t.addCell(totalValue(formatCurrency(
                invoice != null ? invoice.getSgst() : null)));

        // Grand Total highlighted bar
        Cell grandL = noBorderCell();
        grandL.setBackgroundColor(THEME_BLUE);
        grandL.setPadding(10);
        grandL.add(new Paragraph("GRAND TOTAL")
                .setBold().setFontSize(12)
                .setFontColor(ColorConstants.WHITE));

        Cell grandR = noBorderCell();
        grandR.setBackgroundColor(THEME_BLUE);
        grandR.setPadding(10);
        grandR.setTextAlignment(TextAlignment.RIGHT);
        grandR.add(new Paragraph(formatCurrency(
                invoice != null ? invoice.getTotalAmount() : null))
                .setBold().setFontSize(12)
                .setFontColor(ColorConstants.WHITE));

        t.addCell(grandL);
        t.addCell(grandR);
        doc.add(t);

        // Amount in words
        if (invoice != null && invoice.getTotalAmount() != null) {
            doc.add(new Paragraph()
                    .add(kvInline("Amount in Words: ",
                            convertToWords(invoice.getTotalAmount())))
                    .setFontSize(9)
                    .setMarginTop(4)
                    .setMarginBottom(4));
        }
    }

    private void addBankAndTermsBlock(Document doc) {
        Table outer = new Table(new float[]{1f, 1f}).useAllAvailableWidth();
        outer.setMarginTop(10).setMarginBottom(10);

        Cell bank = noBorderCell();
        bank.add(new Paragraph("Bank Details:")
                .setBold().setFontColor(THEME_BLUE)
                .setFontSize(10).setMarginBottom(4));
        bank.add(miniKv("Account Name: ", BANK_ACCOUNT_NAME));
        bank.add(miniKv("Bank: ", BANK_NAME));
        bank.add(miniKv("Branch: ", BANK_BRANCH));
        bank.add(miniKv("Account No: ", BANK_ACCOUNT_NO));
        bank.add(miniKv("IFSC: ", BANK_IFSC));

        Cell terms = noBorderCell();
        terms.add(new Paragraph("Terms & Conditions:")
                .setBold().setFontColor(THEME_BLUE)
                .setFontSize(10).setMarginBottom(4));
        terms.add(miniKv("Payment: ", TERMS_PAYMENT));
        terms.add(miniKv("Delivery: ", TERMS_DELIVERY));
        terms.add(new Paragraph("* Goods once sold will not be taken back.")
                .setFontSize(8).setMarginTop(4));
        terms.add(new Paragraph("* Subject to Kolhapur jurisdiction.")
                .setFontSize(8));

        outer.addCell(bank);
        outer.addCell(terms);
        doc.add(outer);
    }

    private void addInvoiceSignature(Document doc) {
        Table t = new Table(new float[]{1f, 1f}).useAllAvailableWidth();
        t.setMarginTop(10).setMarginBottom(6);

        Cell left = noBorderCell();
        left.add(new Paragraph("Customer Seal & Signature")
                .setFontSize(8).setFontColor(THEME_BLUE).setMarginTop(30));

        Cell right = noBorderCell();
        right.setTextAlignment(TextAlignment.RIGHT);
        right.add(new Paragraph("For " + COMPANY_NAME)
                .setBold().setFontSize(9)
                .setFontColor(THEME_BLUE).setMarginBottom(25));
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
                .setPadding(6)
                .setBorder(new SolidBorder(ColorConstants.WHITE, 0.5f))
                .add(new Paragraph(text).setFontSize(9));
    }

    private Cell bodyCell(String text, TextAlignment align, boolean shaded) {
        Cell cell = new Cell()
                .setTextAlignment(align)
                .setPadding(5)
                .setBorder(new SolidBorder(ROW_BORDER, 0.5f))
                .add(new Paragraph(safe(text)).setFontSize(9));
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
                .setPadding(6)
                .setBorder(new SolidBorder(THEME_BLUE, 0.8f))
                .add(new Paragraph(text).setFontSize(9)
                        .setFontColor(THEME_BLUE));
    }

    private Cell totalLabel(String text) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_LINE, 0.5f))
                .setPadding(8)
                .add(new Paragraph(text)
                        .setFontColor(THEME_BLUE).setBold().setFontSize(10));
    }

    //------------------------------------------------
    // SIGNATURE
    //------------------------------------------------

    private Cell totalValue(String text) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_LINE, 0.5f))
                .setPadding(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(text).setBold().setFontSize(10));
    }

    private Cell noBorderCell() {
        return new Cell().setBorder(Border.NO_BORDER);
    }

    // ════════════════════════════════════════════════════════════
    //  HELPER PARAGRAPHS
    // ════════════════════════════════════════════════════════════

    private Paragraph miniLine(String text) {
        return new Paragraph(safe(text))
                .setFontSize(8).setMargin(0).setMarginBottom(2);
    }

    private Paragraph miniKv(String key, String value) {
        Paragraph p = new Paragraph()
                .setFontSize(8).setMargin(0).setMarginBottom(2);
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
        line.setMarginTop(3);
        line.setMarginBottom(3);
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
        height += 20;   // copy label
        height += 80;   // header
        height += 30;   // title
        height += 80;   // to block
        height += TABLE_HEADER_HEIGHT;
        height += (items != null ? items.size() : 0) * TABLE_ROW_HEIGHT;
        height += 60;   // totals
        height += 80;   // signature
        height += 40;   // margins/rules
        return height;
    }

    // ════════════════════════════════════════════════════════════
    //  DATA FORMATTING  *** FIX IS HERE ***
    // ════════════════════════════════════════════════════════════

    private String formatWeight(BigDecimal weight) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(weight != null ? weight : BigDecimal.ZERO);
    }

    /**
     * FIX: "Rs." contains a dot which DecimalFormat treats as
     * a second decimal separator.
     * Solution: Format the number first, then prepend "Rs. "
     */
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

    /**
     * Number-to-words converter for Indian currency
     */
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
    //  PAGE BORDER + PAGE NUMBER HANDLER  *** FIX IS HERE ***
    // ════════════════════════════════════════════════════════════

    /**
     * FIX: Original code used PdfFontFactory.createRegisteredFont()
     * which throws IOException. Using try-catch and standard font.
     */
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

            // Draw thin border around page
            canvas.setStrokeColor(LIGHT_LINE)
                    .setLineWidth(0.8f)
                    .rectangle(
                            pageSize.getLeft() + 14,
                            pageSize.getBottom() + 10,
                            pageSize.getWidth() - 28,
                            pageSize.getHeight() - 20)
                    .stroke();

            // Page number at bottom center
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