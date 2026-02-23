package com.kalibyte.foundry.quotation.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.*;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.*;
import com.itextpdf.layout.borders.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.kalibyte.foundry.quotation.entity.Quotation;
import com.kalibyte.foundry.quotation.entity.QuotationItem;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;

@Service
public class QuotationPdfService {

    public byte[] generatePdf(Quotation quotation) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // ================= LOGO =================
            try {
                ClassPathResource resource =
                        new ClassPathResource("static/logo.png");
                InputStream inputStream = resource.getInputStream();
                Image logo = new Image(
                        ImageDataFactory.create(inputStream.readAllBytes()));
                logo.setWidth(140);
                logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(logo);
            } catch (Exception ignored) {}

            // ================= COMPANY HEADER =================
            document.add(new Paragraph("MITTAL PRECISION STEEL FOUNDRY")
                    .setBold()
                    .setFontSize(20)
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(
                    "Plot No: A-12, MIDC Industrial Area, Kolhapur - 416234")
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(
                    "GST No: 27AACM1234P125 | Contact: 0214-2654321 | Email: info@mittalfoundry.com")
                    .setTextAlignment(TextAlignment.CENTER));

            addSeparator(document);

            // ================= TOP BLOCK =================
            Table top = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .useAllAvailableWidth();

            top.addCell(new Cell().add(new Paragraph(
                            "To,\n" +
                                    quotation.getCustomer().getName() + "\n" +
                                    quotation.getCustomer().getAddress()))
                    .setBorder(Border.NO_BORDER));

            top.addCell(new Cell().add(new Paragraph(
                            "Quotation No: " + quotation.getQuotationNumber() +
                                    "\nDate: " + quotation.getQuotationDate()))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(Border.NO_BORDER));

            document.add(top);

            document.add(new Paragraph("Quotation")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            addSeparator(document);

            // ================= ITEM TABLE =================
            float[] widths = {1, 4, 2, 2, 2, 2, 3};
            Table table = new Table(UnitValue.createPercentArray(widths))
                    .useAllAvailableWidth();

            addHeader(table, "Sr No");
            addHeader(table, "Description");
            addHeader(table, "Grade");
            addHeader(table, "Weight (Kg)");
            addHeader(table, "Rate / Kg");
            addHeader(table, "Qty");
            addHeader(table, "Total Amount");

            int sr = 1;
            BigDecimal subTotal = BigDecimal.ZERO;

            for (QuotationItem item : quotation.getItems()) {

                BigDecimal total =
                        item.getNetWeightKg()
                                .multiply(item.getUnitPrice())
                                .multiply(item.getQuantity());

                subTotal = subTotal.add(total);

                table.addCell(createCell(String.valueOf(sr++)));
                table.addCell(createCell(item.getPartName()));
                table.addCell(createCell(item.getMaterialGrade()));
                table.addCell(createCell(item.getNetWeightKg().toString()));
                table.addCell(createCell(formatINR(item.getUnitPrice())));
                table.addCell(createCell(item.getQuantity().toString()));
                table.addCell(createCell(formatINR(total)));
            }

            document.add(table);

            document.add(new Paragraph("\nTotal Amount: "
                    + formatINR(subTotal))
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT));

            addSeparator(document);

            // ================= COST BREAKUP =================
            document.add(new Paragraph("Cost Breakup:")
                    .setBold());

            document.add(new Paragraph("• Basic Casting Rate: "
                    + formatINR(subTotal)));

            BigDecimal gst = subTotal.multiply(new BigDecimal("0.18"));
            document.add(new Paragraph("• GST @ 18%: "
                    + formatINR(gst)));

            addSeparator(document);

            // ================= TERMS =================
            document.add(new Paragraph("Terms & Conditions:")
                    .setBold());

            document.add(new Paragraph("• Rate Validity: 30 Days"));
            document.add(new Paragraph("• Delivery Time: 25-30 Days from PO"));
            document.add(new Paragraph("• Payment Terms: "
                    + quotation.getPaymentTerms()));
            document.add(new Paragraph("• GST Extra as Applicable"));

            addSeparator(document);

            // ================= SIGNATURE =================
            Table sign = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .useAllAvailableWidth();

            sign.addCell(new Cell()
                    .add(new Paragraph("For Mittal Precision Steel Foundry"))
                    .setBorder(Border.NO_BORDER));

            Cell rightCell = new Cell().setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT);

            try {
                ClassPathResource signRes =
                        new ClassPathResource("static/signature.png");
                InputStream is = signRes.getInputStream();
                Image signImg = new Image(
                        ImageDataFactory.create(is.readAllBytes()));
                signImg.setWidth(120);
                rightCell.add(signImg);
            } catch (Exception ignored) {}

            rightCell.add(new Paragraph("Authorized Signatory"));
            sign.addCell(rightCell);

            document.add(sign);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private void addHeader(Table table, String text) {
        table.addHeaderCell(
                new Cell()
                        .add(new Paragraph(text).setBold())
                        .setBackgroundColor(ColorConstants.BLUE)
                        .setFontColor(ColorConstants.WHITE)
                        .setBorder(new SolidBorder(1))
        );
    }

    private Cell createCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setBorder(new SolidBorder(1));
    }

    private void addSeparator(Document document) {
        document.add(new LineSeparator(new SolidLine()));
    }

    private String formatINR(BigDecimal amount) {
        long value = amount.longValue();
        String s = String.valueOf(value);
        if (s.length() <= 3) return "₹ " + s;

        String last3 = s.substring(s.length() - 3);
        String remaining = s.substring(0, s.length() - 3);

        StringBuilder sb = new StringBuilder();
        while (remaining.length() > 2) {
            sb.insert(0, "," + remaining.substring(remaining.length() - 2));
            remaining = remaining.substring(0, remaining.length() - 2);
        }
        sb.insert(0, remaining);

        return "₹ " + sb.toString() + last3;
    }
}