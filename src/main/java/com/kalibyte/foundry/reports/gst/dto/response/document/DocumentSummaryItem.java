package com.kalibyte.foundry.reports.gst.dto.response.document;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSummaryItem {

    private String documentType;    // "Invoices", "Credit Notes", "Debit Notes"
    private String fromSerialNo;
    private String toSerialNo;
    private int totalIssued;
    private int totalCancelled;
    private int netIssued;
}