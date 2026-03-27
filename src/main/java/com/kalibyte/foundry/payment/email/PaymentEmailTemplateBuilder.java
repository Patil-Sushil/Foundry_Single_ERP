package com.kalibyte.foundry.payment.email;

import java.math.BigDecimal;
import java.util.List;

/**
 * Builds professional HTML email bodies for payment events.
 *
 * Each event type gets a distinct color theme and highlighted banner
 * so the recipient immediately understands the nature of the email.
 */
public final class PaymentEmailTemplateBuilder {

    private PaymentEmailTemplateBuilder() {
    }

    // ══════════════════════════════════════════════════
    //  COLOR THEMES PER EVENT
    // ══════════════════════════════════════════════════
    private static final String COLOR_SUCCESS    = "#16a34a"; // green
    private static final String COLOR_PENDING    = "#d97706"; // amber
    private static final String COLOR_BOUNCED    = "#dc2626"; // red
    private static final String COLOR_CANCELLED  = "#6b7280"; // gray
    private static final String COLOR_PRIMARY    = "#1e40af"; // brand blue
    private static final String COLOR_BG_LIGHT   = "#f9fafb";
    private static final String COLOR_BORDER     = "#e5e7eb";
    private static final String COLOR_TEXT       = "#374151";
    private static final String COLOR_TEXT_MUTED = "#6b7280";

    // ══════════════════════════════════════════════════
    //  PUBLIC ENTRY POINT
    // ══════════════════════════════════════════════════
    public static String build(PaymentEmailContext ctx) {
        return switch (ctx.getEventType()) {
            case PAYMENT_CREATED  -> buildPaymentCreated(ctx);
            case CHEQUE_CLEARED   -> buildChequeCleared(ctx);
            case CHEQUE_BOUNCED   -> buildChequeBounced(ctx);
            case PAYMENT_CANCELLED -> buildPaymentCancelled(ctx);
        };
    }

    // ══════════════════════════════════════════════════
    //  1. PAYMENT CREATED
    // ══════════════════════════════════════════════════
    private static String buildPaymentCreated(PaymentEmailContext ctx) {

        String bannerColor = ctx.getStatus().equalsIgnoreCase("PENDING")
                ? COLOR_PENDING
                : COLOR_SUCCESS;

        String bannerIcon = ctx.getStatus().equalsIgnoreCase("PENDING")
                ? "⏳" : "✅";

        String bannerText = ctx.getStatus().equalsIgnoreCase("PENDING")
                ? "PAYMENT RECEIVED — PENDING CLEARANCE"
                : "PAYMENT RECEIVED SUCCESSFULLY";

        StringBuilder sb = new StringBuilder();
        sb.append(openWrapper());
        sb.append(banner(bannerColor, bannerIcon, bannerText));
        sb.append(greeting(ctx.getCustomerName()));
        sb.append(paragraph("Thank you! We have received your payment. "
                + "Here are the details:"));

        // Payment details table
        sb.append(sectionHeader("Payment Details"));
        sb.append(openTable());
        sb.append(row("Payment Number", ctx.getPaymentNumber()));
        sb.append(row("Payment Date", ctx.getPaymentDate()));
        sb.append(row("Payment Method", ctx.getPaymentMethod()));
        sb.append(highlightRow("Amount Paid", "₹" + ctx.getAmountPaid(),
                COLOR_PRIMARY));
        sb.append(statusRow("Status", ctx.getStatus(), bannerColor));
        sb.append(closeTable());

        // Method-specific details
        if (ctx.getMethodDetails() != null && !ctx.getMethodDetails().isEmpty()) {
            sb.append(sectionHeader("Transaction Details"));
            sb.append(openTable());
            for (PaymentEmailContext.DetailRow detail : ctx.getMethodDetails()) {
                sb.append(row(detail.getLabel(), detail.getValue()));
            }
            sb.append(closeTable());
        }

        // Pending notice
        if (ctx.getStatus().equalsIgnoreCase("PENDING")) {
            sb.append(alertBox(COLOR_PENDING,
                    "⏳ PENDING CLEARANCE",
                    "This payment is currently pending clearance. "
                            + "The amount will be credited to your account once the "
                            + ctx.getPaymentMethod()
                            + " is processed by the bank. "
                            + "We will notify you upon clearance."));
        }

        // Invoice summary
        sb.append(invoiceSummary(ctx));

        sb.append(footer());
        sb.append(closeWrapper());
        return sb.toString();
    }

    // ══════════════════════════════════════════════════
    //  2. CHEQUE CLEARED
    // ══════════════════════════════════════════════════
    private static String buildChequeCleared(PaymentEmailContext ctx) {

        String instrumentType = ctx.getPaymentMethod()
                .toUpperCase().contains("CHEQUE")
                ? "Cheque" : "Demand Draft";

        StringBuilder sb = new StringBuilder();
        sb.append(openWrapper());
        sb.append(banner(COLOR_SUCCESS, "✅",
                instrumentType.toUpperCase() + " CLEARED SUCCESSFULLY"));
        sb.append(greeting(ctx.getCustomerName()));
        sb.append(paragraph(
                "Great news! Your <strong>" + instrumentType
                        + "</strong> has been successfully cleared "
                        + "and the amount has been credited to your account."));

        // Success highlight box
        sb.append(successBox(
                "₹" + ctx.getAmountPaid() + " CLEARED",
                instrumentType + " No. " + ctx.getInstrumentNumber()
                        + " has been processed successfully on "
                        + ctx.getEventDate() + "."));

        // Instrument details
        sb.append(sectionHeader(instrumentType + " Details"));
        sb.append(openTable());
        sb.append(row("Payment Number", ctx.getPaymentNumber()));
        sb.append(row(instrumentType + " Number", ctx.getInstrumentNumber()));
        sb.append(row(instrumentType + " Date", ctx.getInstrumentDate()));
        sb.append(highlightRow("Amount", "₹" + ctx.getAmountPaid(),
                COLOR_SUCCESS));
        sb.append(row("Bank", ctx.getBankName()));
        sb.append(row("Branch", nvl(ctx.getBranchName())));
        sb.append(row("Cleared On", ctx.getEventDate()));
        sb.append(statusRow("Status", "CLEARED", COLOR_SUCCESS));
        sb.append(closeTable());

        // Invoice summary
        sb.append(invoiceSummary(ctx));

        sb.append(footer());
        sb.append(closeWrapper());
        return sb.toString();
    }

    // ══════════════════════════════════════════════════
    //  3. CHEQUE BOUNCED
    // ══════════════════════════════════════════════════
    private static String buildChequeBounced(PaymentEmailContext ctx) {

        String instrumentType = ctx.getPaymentMethod()
                .toUpperCase().contains("CHEQUE")
                ? "Cheque" : "Demand Draft";

        StringBuilder sb = new StringBuilder();
        sb.append(openWrapper());
        sb.append(banner(COLOR_BOUNCED, "❌",
                instrumentType.toUpperCase() + " BOUNCED"));
        sb.append(greeting(ctx.getCustomerName()));
        sb.append(paragraph(
                "We regret to inform you that your <strong>"
                        + instrumentType
                        + "</strong> has been <strong style=\"color:"
                        + COLOR_BOUNCED
                        + "\">returned unpaid (bounced)</strong> by the bank."));

        // BOUNCED ALERT BOX — highly visible
        sb.append(alertBox(COLOR_BOUNCED,
                "❌ " + instrumentType.toUpperCase() + " BOUNCED",
                "Your " + instrumentType + " No. <strong>"
                        + ctx.getInstrumentNumber()
                        + "</strong> for <strong>₹"
                        + ctx.getAmountPaid()
                        + "</strong> has been dishonoured."
                        + "<br/><strong>Reason: "
                        + ctx.getCancellationReason()
                        + "</strong>"));

        // Instrument details
        sb.append(sectionHeader(instrumentType + " Details"));
        sb.append(openTable());
        sb.append(row("Payment Number", ctx.getPaymentNumber()));
        sb.append(row(instrumentType + " Number", ctx.getInstrumentNumber()));
        sb.append(row(instrumentType + " Date", ctx.getInstrumentDate()));
        sb.append(highlightRow("Amount", "₹" + ctx.getAmountPaid(),
                COLOR_BOUNCED));
        sb.append(row("Bank", ctx.getBankName()));
        sb.append(row("Branch", nvl(ctx.getBranchName())));
        sb.append(row("Bounce Reason", ctx.getCancellationReason()));
        sb.append(row("Bounced On", ctx.getEventDate()));
        sb.append(statusRow("Status", "BOUNCED", COLOR_BOUNCED));
        sb.append(closeTable());

        // Invoice summary
        sb.append(invoiceSummary(ctx));

        // ACTION REQUIRED box
        sb.append(actionRequiredBox(ctx));

        sb.append(footer());
        sb.append(closeWrapper());
        return sb.toString();
    }

    // ══════════════════════════════════════════════════
    //  4. PAYMENT CANCELLED
    // ══════════════════════════════════════════════════
    private static String buildPaymentCancelled(PaymentEmailContext ctx) {

        StringBuilder sb = new StringBuilder();
        sb.append(openWrapper());
        sb.append(banner(COLOR_CANCELLED, "🚫", "PAYMENT CANCELLED"));
        sb.append(greeting(ctx.getCustomerName()));
        sb.append(paragraph(
                "This is to inform you that the following payment "
                        + "has been <strong>cancelled</strong>."));

        sb.append(alertBox(COLOR_CANCELLED,
                "🚫 CANCELLATION NOTICE",
                "Payment <strong>" + ctx.getPaymentNumber()
                        + "</strong> for <strong>₹"
                        + ctx.getAmountPaid()
                        + "</strong> has been cancelled."
                        + "<br/>Reason: <strong>"
                        + ctx.getCancellationReason()
                        + "</strong>"));

        // Payment details
        sb.append(sectionHeader("Cancelled Payment Details"));
        sb.append(openTable());
        sb.append(row("Payment Number", ctx.getPaymentNumber()));
        sb.append(row("Payment Date", ctx.getPaymentDate()));
        sb.append(row("Payment Method", ctx.getPaymentMethod()));
        sb.append(highlightRow("Amount", "₹" + ctx.getAmountPaid(),
                COLOR_CANCELLED));
        sb.append(row("Cancel Reason", ctx.getCancellationReason()));
        sb.append(row("Cancelled On", ctx.getEventDate()));
        sb.append(statusRow("Status", "CANCELLED", COLOR_CANCELLED));
        sb.append(closeTable());

        // Method-specific details
        if (ctx.getMethodDetails() != null && !ctx.getMethodDetails().isEmpty()) {
            sb.append(sectionHeader("Transaction Details"));
            sb.append(openTable());
            for (PaymentEmailContext.DetailRow detail : ctx.getMethodDetails()) {
                sb.append(row(detail.getLabel(), detail.getValue()));
            }
            sb.append(closeTable());
        }

        // Invoice summary
        sb.append(invoiceSummary(ctx));

        sb.append(paragraph(
                "If this cancellation was not expected, or if you "
                        + "need to arrange an alternative payment, please contact "
                        + "our accounts team."));

        sb.append(footer());
        sb.append(closeWrapper());
        return sb.toString();
    }

    // ══════════════════════════════════════════════════════════════
    //  REUSABLE HTML COMPONENTS
    // ══════════════════════════════════════════════════════════════

    private static String openWrapper() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            </head>
            <body style="margin:0; padding:0; background-color:#f3f4f6;
                         font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0"
                     style="background-color:#f3f4f6;">
                <tr>
                  <td align="center" style="padding: 24px 16px;">
                    <table role="presentation" width="600" cellspacing="0" cellpadding="0"
                           style="background-color:#ffffff;
                                  border-radius: 8px;
                                  overflow: hidden;
                                  box-shadow: 0 1px 3px rgba(0,0,0,0.1);">
            """;
    }

    private static String closeWrapper() {
        return """
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """;
    }

    private static String banner(String bgColor, String icon, String text) {
        return """
            <tr>
              <td style="background-color: %s; padding: 28px 32px; text-align: center;">
                <div style="font-size: 36px; margin-bottom: 8px;">%s</div>
                <div style="color: #ffffff; font-size: 18px; font-weight: 700;
                            letter-spacing: 1px; text-transform: uppercase;">
                  %s
                </div>
              </td>
            </tr>
            """.formatted(bgColor, icon, text);
    }

    private static String greeting(String name) {
        return """
            <tr>
              <td style="padding: 28px 32px 0 32px;">
                <p style="margin: 0; font-size: 16px; color: %s;">
                  Dear <strong>%s</strong>,
                </p>
              </td>
            </tr>
            """.formatted(COLOR_TEXT, name);
    }

    private static String paragraph(String text) {
        return """
            <tr>
              <td style="padding: 12px 32px 0 32px;">
                <p style="margin: 0; font-size: 14px; line-height: 1.6;
                          color: %s;">
                  %s
                </p>
              </td>
            </tr>
            """.formatted(COLOR_TEXT, text);
    }

    private static String sectionHeader(String title) {
        return """
            <tr>
              <td style="padding: 24px 32px 8px 32px;">
                <p style="margin: 0; font-size: 13px; font-weight: 700;
                          color: %s; text-transform: uppercase;
                          letter-spacing: 0.5px;
                          border-bottom: 2px solid %s;
                          padding-bottom: 6px;">
                  %s
                </p>
              </td>
            </tr>
            """.formatted(COLOR_PRIMARY, COLOR_BORDER, title);
    }

    private static String openTable() {
        return """
            <tr>
              <td style="padding: 0 32px;">
                <table role="presentation" width="100%%" cellspacing="0"
                       cellpadding="0"
                       style="border: 1px solid %s; border-radius: 6px;
                              overflow: hidden;">
            """.formatted(COLOR_BORDER);
    }

    private static String closeTable() {
        return """
                </table>
              </td>
            </tr>
            """;
    }

    private static String row(String label, String value) {
        return """
            <tr>
              <td style="padding: 10px 16px; font-size: 13px;
                         color: %s; width: 40%%;
                         background-color: %s;
                         border-bottom: 1px solid %s;">
                %s
              </td>
              <td style="padding: 10px 16px; font-size: 13px;
                         color: %s; font-weight: 500;
                         border-bottom: 1px solid %s;">
                %s
              </td>
            </tr>
            """.formatted(
                COLOR_TEXT_MUTED, COLOR_BG_LIGHT, COLOR_BORDER, label,
                COLOR_TEXT, COLOR_BORDER, value != null ? value : "N/A");
    }

    private static String highlightRow(
            String label, String value, String valueColor) {
        return """
            <tr>
              <td style="padding: 10px 16px; font-size: 13px;
                         color: %s; width: 40%%;
                         background-color: %s;
                         border-bottom: 1px solid %s;">
                %s
              </td>
              <td style="padding: 10px 16px; font-size: 16px;
                         color: %s; font-weight: 700;
                         border-bottom: 1px solid %s;">
                %s
              </td>
            </tr>
            """.formatted(
                COLOR_TEXT_MUTED, COLOR_BG_LIGHT, COLOR_BORDER, label,
                valueColor, COLOR_BORDER, value);
    }

    private static String statusRow(
            String label, String status, String badgeColor) {
        return """
            <tr>
              <td style="padding: 10px 16px; font-size: 13px;
                         color: %s; width: 40%%;
                         background-color: %s;
                         border-bottom: 1px solid %s;">
                %s
              </td>
              <td style="padding: 10px 16px;
                         border-bottom: 1px solid %s;">
                <span style="display: inline-block;
                             padding: 4px 14px;
                             font-size: 12px;
                             font-weight: 700;
                             color: #ffffff;
                             background-color: %s;
                             border-radius: 20px;
                             text-transform: uppercase;
                             letter-spacing: 0.5px;">
                  %s
                </span>
              </td>
            </tr>
            """.formatted(
                COLOR_TEXT_MUTED, COLOR_BG_LIGHT, COLOR_BORDER, label,
                COLOR_BORDER, badgeColor, status);
    }

    private static String alertBox(
            String borderColor, String title, String message) {
        return """
            <tr>
              <td style="padding: 20px 32px;">
                <table role="presentation" width="100%%" cellspacing="0"
                       cellpadding="0"
                       style="border-left: 4px solid %s;
                              background-color: %s;
                              border-radius: 0 6px 6px 0;">
                  <tr>
                    <td style="padding: 16px 20px;">
                      <p style="margin: 0 0 6px 0; font-size: 14px;
                                font-weight: 700; color: %s;">
                        %s
                      </p>
                      <p style="margin: 0; font-size: 13px;
                                line-height: 1.5; color: %s;">
                        %s
                      </p>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
            """.formatted(
                borderColor,
                hexToRgba(borderColor, 0.06),
                borderColor, title,
                COLOR_TEXT, message);
    }

    private static String successBox(String title, String message) {
        return """
            <tr>
              <td style="padding: 20px 32px;">
                <table role="presentation" width="100%%" cellspacing="0"
                       cellpadding="0"
                       style="background-color: #f0fdf4;
                              border: 1px solid #bbf7d0;
                              border-radius: 8px;">
                  <tr>
                    <td style="padding: 20px; text-align: center;">
                      <div style="font-size: 32px; margin-bottom: 8px;">✅</div>
                      <p style="margin: 0 0 4px 0; font-size: 18px;
                                font-weight: 700; color: %s;">
                        %s
                      </p>
                      <p style="margin: 0; font-size: 13px; color: %s;">
                        %s
                      </p>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
            """.formatted(COLOR_SUCCESS, title, COLOR_TEXT, message);
    }

    private static String actionRequiredBox(PaymentEmailContext ctx) {
        return """
            <tr>
              <td style="padding: 20px 32px;">
                <table role="presentation" width="100%%" cellspacing="0"
                       cellpadding="0"
                       style="background-color: #fef2f2;
                              border: 2px solid %s;
                              border-radius: 8px;">
                  <tr>
                    <td style="padding: 20px;">
                      <p style="margin: 0 0 8px 0; font-size: 15px;
                                font-weight: 700; color: %s;
                                text-align: center;">
                        ⚠️ ACTION REQUIRED
                      </p>
                      <p style="margin: 0 0 12px 0; font-size: 13px;
                                line-height: 1.6; color: %s;">
                        The bounced amount of <strong>₹%s</strong>
                        has been reversed from your account. Please
                        arrange an alternative payment at the earliest
                        to avoid any service disruption.
                      </p>
                      <p style="margin: 0 0 4px 0; font-size: 13px;
                                font-weight: 600; color: %s;">
                        Accepted Payment Methods:
                      </p>
                      <ul style="margin: 4px 0 0 0; padding-left: 20px;
                                 font-size: 13px; color: %s;
                                 line-height: 1.8;">
                        <li>UPI</li>
                        <li>NEFT / RTGS / IMPS</li>
                        <li>Cash</li>
                        <li>New Cheque / Demand Draft</li>
                      </ul>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
            """.formatted(
                COLOR_BOUNCED, COLOR_BOUNCED,
                COLOR_TEXT, ctx.getAmountPaid(),
                COLOR_TEXT, COLOR_TEXT);
    }

    private static String invoiceSummary(PaymentEmailContext ctx) {

        String statusColor = switch (ctx.getInvoiceStatus().toUpperCase()) {
            case "PAID"           -> COLOR_SUCCESS;
            case "PARTIALLY_PAID" -> COLOR_PENDING;
            case "CANCELLED"      -> COLOR_CANCELLED;
            default               -> COLOR_BOUNCED;
        };

        boolean fullyPaid = ctx.getRemainingAmount()
                .compareTo(BigDecimal.ZERO) <= 0
                && ctx.getTotalPending()
                .compareTo(BigDecimal.ZERO) <= 0;

        StringBuilder sb = new StringBuilder();
        sb.append(sectionHeader("Invoice Summary"));
        sb.append(openTable());
        sb.append(row("Invoice Number", ctx.getInvoiceNumber()));
        sb.append(row("Invoice Amount", "₹" + ctx.getInvoiceAmount()));
        sb.append(highlightRow("Total Paid", "₹" + ctx.getTotalPaid(),
                COLOR_SUCCESS));

        if (ctx.getTotalPending().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(highlightRow("Pending Clearance",
                    "₹" + ctx.getTotalPending(), COLOR_PENDING));
        }

        sb.append(highlightRow("Balance Due",
                "₹" + ctx.getRemainingAmount(),
                ctx.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0
                        ? COLOR_BOUNCED : COLOR_SUCCESS));
        sb.append(statusRow("Invoice Status",
                ctx.getInvoiceStatus(), statusColor));
        sb.append(closeTable());

        if (fullyPaid) {
            sb.append("""
                <tr>
                  <td style="padding: 16px 32px; text-align: center;">
                    <span style="display: inline-block; padding: 8px 24px;
                                 background-color: #f0fdf4;
                                 color: %s;
                                 font-size: 14px; font-weight: 700;
                                 border-radius: 6px;
                                 border: 1px solid #bbf7d0;">
                      🎉 INVOICE FULLY PAID
                    </span>
                  </td>
                </tr>
                """.formatted(COLOR_SUCCESS));
        }

        return sb.toString();
    }

    private static String footer() {
        return """
            <tr>
              <td style="padding: 24px 32px 8px 32px;">
                <hr style="border: none; border-top: 1px solid %s;"/>
              </td>
            </tr>
            <tr>
              <td style="padding: 8px 32px 28px 32px; text-align: center;">
                <p style="margin: 0 0 4px 0; font-size: 12px; color: %s;">
                  For queries, contact our accounts team.
                </p>
                <p style="margin: 0; font-size: 13px; font-weight: 600;
                          color: %s;">
                  Kalibyte Foundry — Accounts Department
                </p>
                <p style="margin: 4px 0 0 0; font-size: 11px; color: %s;">
                  This is an automated email. Please do not reply directly.
                </p>
              </td>
            </tr>
            """.formatted(COLOR_BORDER, COLOR_TEXT_MUTED,
                COLOR_PRIMARY, COLOR_TEXT_MUTED);
    }

    // ── UTILITY ──

    private static String nvl(String v) {
        return v != null && !v.isBlank() ? v : "N/A";
    }

    /**
     * Converts a hex color like "#dc2626" to an rgba() string for backgrounds.
     */
    private static String hexToRgba(String hex, double alpha) {
        String clean = hex.replace("#", "");
        int r = Integer.parseInt(clean.substring(0, 2), 16);
        int g = Integer.parseInt(clean.substring(2, 4), 16);
        int b = Integer.parseInt(clean.substring(4, 6), 16);
        return "rgba(" + r + "," + g + "," + b + "," + alpha + ")";
    }
}