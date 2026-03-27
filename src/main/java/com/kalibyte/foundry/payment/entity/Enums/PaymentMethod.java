package com.kalibyte.foundry.payment.entity.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

    CASH("Cash", false, false, false),
    UPI("UPI", true, false, false),
    BANK_TRANSFER("Bank Transfer", true, false, false),
    CHEQUE("Cheque", false, true, true),
    CARD("Card", true, false, false),
    DEMAND_DRAFT("Demand Draft", false, true, true),
    NEFT("NEFT", true, false, false),
    RTGS("RTGS", true, false, false),
    IMPS("IMPS", true, false, false);

    private final String displayName;
    private final boolean transactionIdRequired;
    private final boolean instrumentNumberRequired;
    private final boolean instrumentDateRequired;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Accepts: "UPI", "upi", "Cash", "CASH", "Bank Transfer", "BANK_TRANSFER", etc.
     */
    @JsonCreator
    public static PaymentMethod fromValue(String value) {
        if (value == null) return null;

        // First try exact displayName match
        for (PaymentMethod method : values()) {
            if (method.displayName.equalsIgnoreCase(value)) {
                return method;
            }
        }

        // Then try enum name match (handles BANK_TRANSFER, DEMAND_DRAFT etc.)
        for (PaymentMethod method : values()) {
            if (method.name().equalsIgnoreCase(value)) {
                return method;
            }
        }

        throw new IllegalArgumentException(
                "Unknown payment method: '" + value
                        + "'. Allowed values: CASH, UPI, BANK_TRANSFER, CHEQUE, CARD, DEMAND_DRAFT, NEFT, RTGS, IMPS"
        );
    }
}