package com.kalibyte.foundry.payment.entity.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    PENDING("Pending"),
    SUCCESS("Success"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded"),
    BOUNCED("Bounced");

    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Accepts: "Pending", "PENDING", "pending", "Success", "SUCCESS", etc.
     */
    @JsonCreator
    public static PaymentStatus fromValue(String value) {
        if (value == null) return null;

        // First try exact displayName match
        for (PaymentStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }

        // Then try enum name match
        for (PaymentStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException(
                "Unknown payment status: '" + value
                        + "'. Allowed values: PENDING, SUCCESS, FAILED, CANCELLED, REFUNDED, BOUNCED"
        );
    }
}