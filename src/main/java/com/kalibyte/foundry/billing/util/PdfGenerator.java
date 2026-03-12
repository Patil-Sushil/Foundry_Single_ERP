package com.kalibyte.foundry.billing.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.DashedLine;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
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

    // Theme colors (match screenshots)
    private static final DeviceRgb THEME_BLUE = new DeviceRgb(18, 53, 102);
    private static final DeviceRgb LIGHT_LINE = new DeviceRgb(200, 210, 225);
    private static final DeviceRgb ROW_BORDER = new DeviceRgb(160, 175, 195);

    // Company constants (as in images) - change as needed
    private static final String COMPANY_NAME = "MITTAL PRECISION STEEL FOUNDRY";
    private static final String COMPANY_PLOT = "Plot No: A-12, MIDC Industrial Area, Kolhapur - 416234";
    private static final String COMPANY_GST = "GST No: 27AACM1234P125";
    private static final String COMPANY_CONTACT = "Contact No: 0214-2654321";
    private static final String COMPANY_EMAIL = "Email: info@mittalfoundry.com";

    // Invoice static blocks (because your Invoice entity fields are not shown for these in code)
    // Replace with your DB values if available.
    private static final String BANK_ACCOUNT_NAME = "Mittal Precision Steel Foundry";
    private static final String BANK_NAME = "HDFC Bank";
    private static final String BANK_BRANCH = "Shiroli (Kolhapur)";
    private static final String BANK_ACCOUNT_NO = "5010012345678";
    private static final String BANK_IFSC = "HDFC0000123";

    private static final String TERMS_PAYMENT = "50% Advance, 50% Before Dispatch";
    private static final String TERMS_DELIVERY = "Mumbai";

    // ----------------------------
    // DELIVERY CHALLAN PDF
    // ----------------------------
    public byte[] generateDeliveryChallanPdf(DeliveryChallan dc, List<DeliveryChallanItem> items) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);

            // Margins similar to your sample
            doc.setMargins(18, 28, 18, 28);

            // Top thin line
            addRule(doc, 1f, LIGHT_LINE);

            // Company title
            doc.add(new Paragraph(COMPANY_NAME)
                    .setBold()
                    .setFontSize(22)
                    .setFontColor(THEME_BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(8));

            // Header block (Left: company info, Right: DC meta)
            addTopHeaderBlockForDc(doc, dc);

            // Line under header
            addRule(doc, 1f, LIGHT_LINE);

            // Center title with lines (as in screenshot)
            addCenteredTitleWithRules(doc, "DELIVERY CHALLAN");

            // To + DC details section
            addDcInfoSectionLikeSample(doc, dc);

            // Items table
            addRule(doc, 1f, LIGHT_LINE);
            addDcItemsTableLikeSample(doc, items);

            // Totals row (Total Qty / Total Weight)
            addDcTotalsRow(doc, items);

            // Divider + (CUT COPY) second table (same as requested)
            addRule(doc, 1f, LIGHT_LINE);
            addCutHereHint(doc);                 // optional (subtle dashed line)
            addDcItemsTableLikeSample(doc, items);

            // Footer note
            addRule(doc, 1f, LIGHT_LINE);
            doc.add(new Paragraph("Material delivered in good condition.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(12)
                    .setMarginBottom(12));

            // Signature
            addRule(doc, 1f, LIGHT_LINE);
            addDcSignatureLikeSample(doc);

            // Bottom line (like the sample has multiple lines; keeping one clean line)
            addRule(doc, 1f, LIGHT_LINE);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating DC PDF", e);
        }
    }

    // ----------------------------
    // INVOICE PDF
    // ----------------------------
    public byte[] generateInvoicePdf(Invoice invoice, List<InvoiceItem> items) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);

            doc.setMargins(18, 28, 18, 28);

            addRule(doc, 1f, LIGHT_LINE);

            doc.add(new Paragraph(COMPANY_NAME)
                    .setBold()
                    .setFontSize(22)
                    .setFontColor(THEME_BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(8));

            // Header block (Left: company info, Right: Invoice meta)
            addTopHeaderBlockForInvoice(doc, invoice);

            addRule(doc, 1f, LIGHT_LINE);

            // Title "Invoice" centered + underlined (as in image)
            Paragraph t = new Paragraph("Invoice")
                    .setBold()
                    .setFontSize(18)
                    .setFontColor(THEME_BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setUnderline();
            doc.add(t.setMarginTop(10).setMarginBottom(10));

            // To block
            addInvoiceToBlockLikeSample(doc, invoice);

            addRule(doc, 1f, LIGHT_LINE);

            // Subject line
            doc.add(new Paragraph()
                    .add(kvBlue("Subject: ", "Invoice for SG Iron Castings"))
                    .setMarginTop(10)
                    .setMarginBottom(10));

            addRule(doc, 1f, LIGHT_LINE);

            // Items table
            addInvoiceItemsTableLikeSample(doc, items);

            // Totals block (styled like image)
            addInvoiceTotalsBlockLikeSample(doc, invoice);

            addRule(doc, 1f, LIGHT_LINE);

            // Bank details + Terms & Conditions
            addBankAndTermsBlockLikeSample(doc);

            addRule(doc, 1f, LIGHT_LINE);

            // Signature block (right aligned)
            addInvoiceSignatureLikeSample(doc);

            addRule(doc, 1f, LIGHT_LINE);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating Invoice PDF", e);
        }
    }

    // =========================================================
    // DC: TOP HEADER (company info left, dc meta right)
    // =========================================================
    private void addTopHeaderBlockForDc(Document doc, DeliveryChallan dc) {
        Table header = new Table(new float[]{2.2f, 1.8f}).useAllAvailableWidth();
        header.setMarginBottom(6);

        Cell left = new Cell().setBorder(Border.NO_BORDER);
        left.add(companyLine(COMPANY_PLOT));
        left.add(companyLine(COMPANY_GST));
        left.add(companyLine(COMPANY_CONTACT));
        left.add(companyLine(COMPANY_EMAIL));

        Cell right = new Cell().setBorder(Border.NO_BORDER);

        right.add(kvLine("Delivery Challan No: ", safe(dc != null ? dc.getDcNumber() : null)));
        right.add(kvLine("Date: ", formatDate(dc != null ? dc.getDispatchDate() : null)));
        right.add(kvLine("Vehicle No: ", safe(dc != null ? dc.getVehicleNumber() : null)));
        right.add(kvLine("Transporter Name: ", safe(dc != null ? dc.getTransportName() : null)));

        header.addCell(left);
        header.addCell(right);

        doc.add(header);
    }

    // =========================================================
    // Invoice: TOP HEADER (company info left, invoice meta right)
    // =========================================================
    private void addTopHeaderBlockForInvoice(Document doc, Invoice invoice) {
        Table header = new Table(new float[]{2.2f, 1.8f}).useAllAvailableWidth();
        header.setMarginBottom(6);

        Cell left = new Cell().setBorder(Border.NO_BORDER);
        left.add(companyLine(COMPANY_PLOT));
        left.add(companyLine(COMPANY_GST));
        left.add(companyLine(COMPANY_CONTACT));
        left.add(companyLine(COMPANY_EMAIL));

        Cell right = new Cell().setBorder(Border.NO_BORDER);
        right.add(kvLine("Invoice No: ", safe(invoice != null ? invoice.getInvoiceNumber() : null)));
        right.add(kvLine("Date: ", formatDate(invoice != null ? invoice.getInvoiceDate() : null)));

        header.addCell(left);
        header.addCell(right);

        doc.add(header);
    }

    // =========================================================
    // Center title with divider lines above & below (DC style)
    // =========================================================
    private void addCenteredTitleWithRules(Document doc, String title) {
        addRule(doc, 1f, LIGHT_LINE);
        doc.add(new Paragraph(title)
                .setBold()
                .setFontSize(18)
                .setFontColor(THEME_BLUE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(8)
                .setMarginBottom(8));
        addRule(doc, 1f, LIGHT_LINE);
    }

    // =========================================================
    // DC: To + Details section (two columns)
    // =========================================================
    private void addDcInfoSectionLikeSample(Document doc, DeliveryChallan dc) {
        Table table = new Table(new float[]{2.2f, 1.8f}).useAllAvailableWidth();
        table.setMarginTop(10).setMarginBottom(10);

        Cell left = new Cell().setBorder(Border.NO_BORDER);
        left.add(new Paragraph("To,").setBold().setFontColor(THEME_BLUE).setMarginBottom(6));
        left.add(new Paragraph(safe(dc != null && dc.getCustomer() != null ? dc.getCustomer().getName() : null))
                .setBold()
                .setMarginBottom(2));
        left.add(new Paragraph(safe(dc != null && dc.getCustomer() != null ? dc.getCustomer().getAddress() : null))
                .setMarginBottom(6));
        left.add(new Paragraph().add(kvBlue("GST No: ", safe(dc != null && dc.getCustomer() != null ? dc.getCustomer().getGstNumber() : null)))
                .setMarginBottom(4));
        left.add(new Paragraph().add(kvBlue("Mobile: ", safe(dc != null && dc.getCustomer() != null ? dc.getCustomer().getPhone() : null))));

        Cell right = new Cell().setBorder(Border.NO_BORDER);
        right.add(kvLine("Delivery Challan No: ", safe(dc != null ? dc.getDcNumber() : null)));
        right.add(kvLine("Vehicle No: ", safe(dc != null ? dc.getVehicleNumber() : null)));
        right.add(kvLine("Transporter Name: ", safe(dc != null ? dc.getTransportName() : null)));
        String pos = (dc != null && dc.getOrder() != null) ? safe(dc.getOrder().getPlaceOfSupply()) : "-";
        right.add(kvLine("Place of Supply: ", pos));

        table.addCell(left);
        table.addCell(right);

        doc.add(table);
    }

    // =========================================================
    // Invoice: "To" block (as screenshot - single left block)
    // =========================================================
    private void addInvoiceToBlockLikeSample(Document doc, Invoice invoice) {
        Table t = new Table(new float[]{1f}).useAllAvailableWidth();
        t.setMarginTop(10).setMarginBottom(10);

        Cell c = new Cell().setBorder(Border.NO_BORDER);
        c.add(new Paragraph("To,").setBold().setFontColor(THEME_BLUE).setMarginBottom(6));
        c.add(new Paragraph(safe(invoice != null && invoice.getCustomer() != null ? invoice.getCustomer().getName() : null))
                .setBold()
                .setMarginBottom(2));
        c.add(new Paragraph(safe(invoice != null && invoice.getCustomer() != null ? invoice.getCustomer().getAddress() : null))
                .setMarginBottom(6));
        c.add(new Paragraph().add(kvBlue("GST No: ", safe(invoice != null && invoice.getCustomer() != null ? invoice.getCustomer().getGstNumber() : null)))
                .setMarginBottom(4));
        // If you have contact person in entity, add it here. (Not used to avoid compilation errors.)
        c.add(new Paragraph().add(kvBlue("Mobile: ", safe(invoice != null && invoice.getCustomer() != null ? invoice.getCustomer().getPhone() : null))));

        t.addCell(c);
        doc.add(t);
    }

    // =========================================================
    // DC: Items table (with Heat No column)
    // =========================================================
    private void addDcItemsTableLikeSample(Document doc, List<DeliveryChallanItem> items) {
        float[] cols = {1.1f, 4.8f, 1.6f, 1.8f, 1.2f, 1.8f};
        Table table = new Table(cols).useAllAvailableWidth();
        table.setMarginTop(10);

        String[] headers = {"Sr No", "Description", "Grade", "Weight (Kg)", "Qty", "Heat No."};
        for (String h : headers) {
            table.addHeaderCell(headerCell(h));
        }

        int sr = 1;
        if (items != null) {
            for (DeliveryChallanItem item : items) {
                table.addCell(bodyCell(String.valueOf(sr++), TextAlignment.CENTER));
                table.addCell(bodyCell(safe(item != null && item.getOrderItem() != null ? item.getOrderItem().getProductName() : null), TextAlignment.LEFT));
                table.addCell(bodyCell("FG 260", TextAlignment.CENTER));
                table.addCell(bodyCell(formatWeight(item != null ? item.getWeight() : null), TextAlignment.RIGHT));
                table.addCell(bodyCell(String.valueOf(item != null ? item.getQuantity() : 0), TextAlignment.CENTER));
                table.addCell(bodyCell(resolveHeatNo(item), TextAlignment.CENTER));
            }
        }

        doc.add(table);
    }

    // Totals row below DC table
    private void addDcTotalsRow(Document doc, List<DeliveryChallanItem> items) {
        int totalQty = 0;
        BigDecimal totalWeight = BigDecimal.ZERO;

        if (items != null) {
            for (DeliveryChallanItem it : items) {
                if (it != null) {
                    totalQty += it.getQuantity();
                    if (it.getWeight() != null) totalWeight = totalWeight.add(it.getWeight());
                }
            }
        }

        Table totals = new Table(new float[]{1f, 1f}).useAllAvailableWidth();
        totals.setMarginTop(10).setMarginBottom(10);

        Cell l = new Cell().setBorder(Border.NO_BORDER);
        l.add(new Paragraph().add(new Paragraph("Total Quantity:").setBold().setFontColor(THEME_BLUE)));
        l.add(new Paragraph(String.valueOf(totalQty)).setBold());

        Cell r = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        r.add(new Paragraph().add(new Paragraph("Total Weight:").setBold().setFontColor(THEME_BLUE)));
        r.add(new Paragraph(formatWeight(totalWeight) + " Kg").setBold());

        totals.addCell(l);
        totals.addCell(r);

        doc.add(totals);
    }

    // Optional dashed "cut here" hint (subtle)
    private void addCutHereHint(Document doc) {
        LineSeparator dashed = new LineSeparator(new DashedLine(1f));
        dashed.setMarginTop(8);
        dashed.setMarginBottom(8);
        dashed.setStrokeColor(LIGHT_LINE);
        doc.add(dashed);
    }

    // =========================================================
    // Invoice: Items table
    // =========================================================
    private void addInvoiceItemsTableLikeSample(Document doc, List<InvoiceItem> items) {
        float[] cols = {1.0f, 4.5f, 1.7f, 1.8f, 1.5f, 1.0f, 2.0f};
        Table table = new Table(cols).useAllAvailableWidth();
        table.setMarginTop(10);

        String[] headers = {"Sr No", "Description", "Grade", "Weight (Kg)", "Rate", "Qty", "Amount"};
        for (String h : headers) {
            table.addHeaderCell(headerCell(h));
        }

        int sr = 1;
        if (items != null) {
            for (InvoiceItem item : items) {
                table.addCell(bodyCell(String.valueOf(sr++), TextAlignment.CENTER));
                table.addCell(bodyCell(safe(item != null && item.getOrderItem() != null ? item.getOrderItem().getProductName() : null), TextAlignment.LEFT));
                table.addCell(bodyCell("SG 500/7", TextAlignment.CENTER));
                table.addCell(bodyCell(formatWeight(item != null ? item.getWeight() : null), TextAlignment.RIGHT));
                table.addCell(bodyCell(formatCurrency(item != null ? item.getRate() : null), TextAlignment.RIGHT));
                table.addCell(bodyCell(String.valueOf(item != null ? item.getQuantity() : 0), TextAlignment.CENTER));
                table.addCell(bodyCell(formatCurrency(item != null ? item.getAmount() : null), TextAlignment.RIGHT));
            }
        }

        doc.add(table);
    }

    // =========================================================
    // Invoice: Totals block (as screenshot)
    // =========================================================
    private void addInvoiceTotalsBlockLikeSample(Document doc, Invoice invoice) {
        Table t = new Table(new float[]{2f, 1f}).useAllAvailableWidth();
        t.setMarginTop(12);

        // Row style: bottom border line
        t.addCell(totalLabelCell("Total Amount"));
        t.addCell(totalValueCell(formatCurrency(invoice != null ? invoice.getSubtotal() : null)));

        t.addCell(totalLabelCell("CGST @ 9%"));
        t.addCell(totalValueCell(formatCurrency(invoice != null ? invoice.getCgst() : null)));

        t.addCell(totalLabelCell("SGST @ 9%"));
        t.addCell(totalValueCell(formatCurrency(invoice != null ? invoice.getSgst() : null)));

        // Grand total emphasized on right side (like sample)
        Cell grandL = new Cell().setBorder(Border.NO_BORDER);
        grandL.setPaddingTop(10);
        grandL.setPaddingBottom(10);
        grandL.add(new Paragraph(" ").setFontSize(1)); // keeps spacing like the screenshot

        Cell grandR = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        grandR.setPaddingTop(10);
        grandR.setPaddingBottom(10);
        grandR.add(new Paragraph()
                .add(new Paragraph("Grand Total: ").setBold().setFontColor(THEME_BLUE))
                .add(new Paragraph(formatCurrency(invoice != null ? invoice.getTotalAmount() : null)).setBold())
        );

        t.addCell(grandL);
        t.addCell(grandR);

        doc.add(t);
    }

    private Cell totalLabelCell(String text) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_LINE, 1f))
                .setPaddingTop(12)
                .setPaddingBottom(12)
                .add(new Paragraph(text).setFontColor(THEME_BLUE).setBold());
    }

    private Cell totalValueCell(String text) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_LINE, 1f))
                .setPaddingTop(12)
                .setPaddingBottom(12)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(text).setBold());
    }

    // =========================================================
    // Invoice: Bank + Terms block
    // =========================================================
    private void addBankAndTermsBlockLikeSample(Document doc) {
        Table outer = new Table(new float[]{1f, 1f}).useAllAvailableWidth();
        outer.setMarginTop(14).setMarginBottom(14);

        // Left: Bank Details
        Cell bank = new Cell().setBorder(Border.NO_BORDER);
        bank.add(new Paragraph("Bank Details:").setBold().setFontColor(THEME_BLUE).setMarginBottom(8));
        bank.add(infoLine("Account Name: ", BANK_ACCOUNT_NAME));
        bank.add(infoLine("Bank Name: ", BANK_NAME));
        bank.add(infoLine("Branch: ", BANK_BRANCH));
        bank.add(infoLine("Account No: ", BANK_ACCOUNT_NO));
        bank.add(infoLine("IFSC Code: ", BANK_IFSC));

        // Right: Terms & Conditions
        Cell terms = new Cell().setBorder(Border.NO_BORDER);
        terms.add(new Paragraph("Terms & Conditions:").setBold().setFontColor(THEME_BLUE).setMarginBottom(8));
        terms.add(infoLine("Payment Terms: ", TERMS_PAYMENT));
        terms.add(infoLine("Delivery Terms: ", TERMS_DELIVERY));

        outer.addCell(bank);
        outer.addCell(terms);

        doc.add(outer);
    }

    private Paragraph infoLine(String k, String v) {
        return new Paragraph()
                .setMargin(0)
                .setMarginBottom(6)
                .add(new Paragraph(k).setBold().setFontColor(THEME_BLUE))
                .add(new Paragraph(safe(v)));
    }

    // =========================================================
    // Signatures
    // =========================================================
    private void addDcSignatureLikeSample(Document doc) {
        Table t = new Table(new float[]{1f, 1f}).useAllAvailableWidth();
        t.setMarginTop(18).setMarginBottom(10);

        Cell left = new Cell().setBorder(Border.NO_BORDER);
        left.add(new Paragraph("Prepared By:  ____________________")
                .setMarginTop(10));

        Cell right = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        right.add(new Paragraph("For Mittal Precision Steel Foundry")
                .setBold()
                .setFontColor(THEME_BLUE)
                .setMarginBottom(22));
        right.add(new Paragraph("______________________________")
                .setMarginTop(6));

        t.addCell(left);
        t.addCell(right);

        doc.add(t);
    }

    private void addInvoiceSignatureLikeSample(Document doc) {
        Table t = new Table(new float[]{1f}).useAllAvailableWidth();
        t.setMarginTop(14).setMarginBottom(8);

        Cell c = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        c.add(new Paragraph("For Mittal Precision Steel Foundry")
                .setBold()
                .setFontColor(THEME_BLUE)
                .setMarginBottom(22));
        c.add(new Paragraph("______________________________"));

        t.addCell(c);
        doc.add(t);
    }

    // =========================================================
    // Table cell styles (match screenshot)
    // =========================================================
    private Cell headerCell(String text) {
        return new Cell()
                .setBackgroundColor(THEME_BLUE)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setPaddingTop(8)
                .setPaddingBottom(8)
                .setBorder(new SolidBorder(ROW_BORDER, 0.8f))
                .add(new Paragraph(text).setFontSize(10));
    }

    private Cell bodyCell(String text, TextAlignment align) {
        return new Cell()
                .setTextAlignment(align)
                .setPaddingTop(8)
                .setPaddingBottom(8)
                .setPaddingLeft(8)
                .setPaddingRight(8)
                .setBorder(new SolidBorder(ROW_BORDER, 0.8f))
                .add(new Paragraph(safe(text)).setFontSize(10));
    }

    // =========================================================
    // Small helper paragraphs
    // =========================================================
    private Paragraph companyLine(String text) {
        // Blue label-like lines were visible in the image; we keep normal black text here for readability.
        return new Paragraph(safe(text)).setFontSize(10).setMargin(0).setMarginBottom(6);
    }

    private Paragraph kvLine(String key, String value) {
        return new Paragraph()
                .setFontSize(10)
                .setMargin(0)
                .setMarginBottom(6)
                .add(kvBlue(key, value));
    }

    /**
     * Creates a "Key in blue bold" + "Value in normal" inline look.
     */
    private Paragraph kvBlue(String key, String value) {
        Paragraph p = new Paragraph().setMargin(0);
        p.add(new Paragraph(key).setBold().setFontColor(THEME_BLUE));
        p.add(new Paragraph(safe(value)));
        return p;
    }

    // =========================================================
    // Rules / Dividers
    // =========================================================
    private void addRule(Document doc, float thickness, DeviceRgb color) {
        SolidLine solid = new SolidLine(thickness);
        solid.setColor(color);
        LineSeparator line = new LineSeparator(solid);
        line.setMarginTop(6);
        line.setMarginBottom(6);
        doc.add(line);
    }

    // =========================================================
    // Data formatting
    // =========================================================
    private String formatWeight(BigDecimal weight) {
        DecimalFormat df = new DecimalFormat("#,##,##0.00");
        return df.format(weight != null ? weight : BigDecimal.ZERO);
    }

    /**
     * NOTE: If "₹" is not rendered, embed a Unicode TTF font OR replace with "Rs. ".
     */
    private String formatCurrency(BigDecimal amount) {
        DecimalFormat df = new DecimalFormat("₹ #,##,##0.00");
        return df.format(amount != null ? amount : BigDecimal.ZERO);
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    /**
     * Supports LocalDate / java.util.Date / fallback to toString().
     * You can replace it with your own date formatting as per your entity type.
     */
    private String formatDate(Object date) {
        if (date == null) return "-";
        try {
            if (date instanceof LocalDate ld) {
                return ld.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            if (date instanceof Date d) {
                // Basic Date -> dd/MM/yyyy (no timezone complexities here)
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                return sdf.format(d);
            }
        } catch (Exception ignored) {
        }
        return String.valueOf(date);
    }

    /**
     * Heat No. resolution:
     * - If your entity has heatNo field, change this method to return it.
     * - Currently returns "-" to avoid compilation issues.
     */
    private String resolveHeatNo(DeliveryChallanItem item) {
        // Example if you add field later:
        // return safe(item != null ? item.getHeatNo() : null);
        return "-";
    }
}