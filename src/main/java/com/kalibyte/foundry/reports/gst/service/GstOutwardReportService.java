package com.kalibyte.foundry.reports.gst.service;

import com.kalibyte.foundry.reports.gst.dto.request.GstReportRequest;
import com.kalibyte.foundry.reports.gst.dto.response.b2b.Gstr1B2BReport;
import com.kalibyte.foundry.reports.gst.dto.response.b2c.Gstr1B2CReport;
import com.kalibyte.foundry.reports.gst.dto.response.document.DocumentSummaryReport;
import com.kalibyte.foundry.reports.gst.dto.response.hsn.HsnSummaryReport;
import com.kalibyte.foundry.reports.gst.dto.response.salesregister.SalesRegisterReport;
import com.kalibyte.foundry.reports.gst.dto.response.taxliability.TaxLiabilitySummary;

public interface GstOutwardReportService {

    Gstr1B2BReport generateB2BReport(GstReportRequest request);

    Gstr1B2CReport generateB2CLargeReport(GstReportRequest request);

    Gstr1B2CReport generateB2CSmallReport(GstReportRequest request);

    HsnSummaryReport generateHsnSummary(GstReportRequest request);

    DocumentSummaryReport generateDocumentSummary(GstReportRequest request);

    SalesRegisterReport generateSalesRegister(GstReportRequest request);

    TaxLiabilitySummary generateTaxLiabilitySummary(GstReportRequest request);
}