// src/main/java/com/kalibyte/foundry/reports/gst/service/GstExcelExportService.java
package com.kalibyte.foundry.reports.gst.service;

import com.kalibyte.foundry.reports.gst.dto.response.b2b.Gstr1B2BReport;
import com.kalibyte.foundry.reports.gst.dto.response.b2c.Gstr1B2CReport;
import com.kalibyte.foundry.reports.gst.dto.response.hsn.HsnSummaryReport;
import com.kalibyte.foundry.reports.gst.dto.response.salesregister.SalesRegisterReport;
import com.kalibyte.foundry.reports.gst.dto.response.taxliability.TaxLiabilitySummary;

public interface GstExcelExportService {

    byte[] exportB2BExcel(Gstr1B2BReport report);

    byte[] exportB2CExcel(Gstr1B2CReport report);

    byte[] exportHsnSummaryExcel(HsnSummaryReport report);

    byte[] exportSalesRegisterExcel(SalesRegisterReport report);

    byte[] exportTaxLiabilityExcel(TaxLiabilitySummary report);
}