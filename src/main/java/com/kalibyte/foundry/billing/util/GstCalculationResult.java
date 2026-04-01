package com.kalibyte.foundry.billing.util;

import com.kalibyte.foundry.order.entity.enums.GstType;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GstCalculationResult {

    private GstType gstType;
    private BigDecimal gstPercentage;
    private BigDecimal subtotal;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal totalGst;
    private BigDecimal grandTotal;

    /**
     * Calculate GST based on customer/vendor state and company's base state.
     *
     * @param subtotal      base amount before GST
     * @param gstPercentage total GST percentage (e.g. 18)
     * @param partyState    customer/vendor's state
     * @param companyState  company's home state (e.g. "Maharashtra")
     * @return GstCalculationResult with all breakdowns
     */
    public static GstCalculationResult calculate(BigDecimal subtotal,
                                                 BigDecimal gstPercentage,
                                                 String partyState,
                                                 String companyState) {

        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (gstPercentage == null) gstPercentage = BigDecimal.valueOf(18);
        if (companyState == null || companyState.isEmpty()) companyState = "Maharashtra";

        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;
        GstType gstType;

        boolean isSameState = companyState.equalsIgnoreCase(
                partyState != null ? partyState.trim() : "");

        if (isSameState) {
            // Intra-state: split into CGST + SGST
            gstType = GstType.CGST_SGST;
            BigDecimal halfPercent = gstPercentage
                    .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);

            BigDecimal divide = subtotal.multiply(halfPercent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            cgst = divide;
            sgst = divide;
        } else {
            // Interstate: full IGST
            gstType = GstType.IGST;
            igst = subtotal.multiply(gstPercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalGst = cgst.add(sgst).add(igst);
        BigDecimal grandTotal = subtotal.add(totalGst);

        return GstCalculationResult.builder()
                .gstType(gstType)
                .gstPercentage(gstPercentage)
                .subtotal(subtotal)
                .cgst(cgst)
                .sgst(sgst)
                .igst(igst)
                .totalGst(totalGst)
                .grandTotal(grandTotal)
                .build();
    }

    /**
     * Calculate GST based on customer state.
     * Company state is assumed to be "Maharashtra".
     *
     * @param subtotal      base amount before GST
     * @param gstPercentage total GST percentage (e.g. 18)
     * @param customerState customer's state
     * @return GstCalculationResult with all breakdowns
     */
    public static GstCalculationResult calculate(BigDecimal subtotal,
                                                 BigDecimal gstPercentage,
                                                 String customerState) {
        return calculate(subtotal, gstPercentage, customerState, "Maharashtra");
    }
}