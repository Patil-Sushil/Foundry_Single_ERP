package com.kalibyte.foundry.enquiry.entity.enums;

import java.util.Arrays;

public enum MetalType {

    // ================= FERROUS =================
    GREY_CAST_IRON("Grey Cast Iron", MetalCategory.FERROUS),
    SG_IRON("SG Iron", MetalCategory.FERROUS),
    MILD_STEEL("Mild Steel", MetalCategory.FERROUS),
    ALLOY_STEEL("Alloy Steel", MetalCategory.FERROUS),

    // ================ NON FERROUS ===============
    ALUMINUM("Aluminum", MetalCategory.NON_FERROUS),
    BRASS("Brass", MetalCategory.NON_FERROUS),
    BRONZE("Bronze", MetalCategory.NON_FERROUS);

    private final String displayName;
    private final MetalCategory category;

    MetalType(String displayName, MetalCategory category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MetalCategory getCategory() {
        return category;
    }

    // Validate if metal type belongs to category
    public static boolean isValidForCategory(MetalType type, MetalCategory category) {
        return type.getCategory() == category;
    }

    // Get metal types by category
    public static MetalType[] getByCategory(MetalCategory category) {
        return Arrays.stream(values())
                .filter(type -> type.category == category)
                .toArray(MetalType[]::new);
    }
}