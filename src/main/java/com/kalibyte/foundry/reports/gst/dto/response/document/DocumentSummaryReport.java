package com.kalibyte.foundry.reports.gst.dto.response.document;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSummaryReport {

    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String periodDescription;

    private int totalDocumentsIssued;
    private int totalCancelled;
    private int netIssued;

    private List<DocumentSummaryItem> items;
}