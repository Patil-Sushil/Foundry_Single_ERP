package com.kalibyte.foundry.reports.account.dto.response.overdueinvoice.enums;

/**
 * Represents severity levels of overdue invoices
 * based on the number of days past the due date.
 */
public enum OverdueSeverity {

    LOW,        // 1-30 days
    MEDIUM,     // 31-60 days
    HIGH,       // 61-90 days
    CRITICAL    // 90+ days

}
