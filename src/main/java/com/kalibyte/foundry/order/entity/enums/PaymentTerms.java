package com.kalibyte.foundry.order.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentTerms {

    ADVANCE("100% Advance"),
    PARTIAL_ADVANCE("Partial Advance"),
    NET_7("Net 7 Days"),
    NET_15("Net 15 Days"),
    NET_30("Net 30 Days"),
    NET_45("Net 45 Days"),
    NET_60("Net 60 Days"),
    NET_90("Net 90 Days"),
    COD("Cash on Delivery"),
    AGAINST_DELIVERY("Against Delivery"),
    AGAINST_PI("Against Proforma Invoice"),
    LC("Letter of Credit"),
    CREDIT_30("30 Days Credit"),
    CREDIT_60("60 Days Credit"),
    CREDIT_90("90 Days Credit"),
    CUSTOM("Custom Terms");

    private final String displayName;
}