package com.kalibyte.foundry.enquiry.entity.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum MetalCategory {

    FERROUS("Ferrous Metals"),
    NON_FERROUS("Non Ferrous Metals");

    private final String displayName;

    MetalCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Get all metal types belonging to this category
    public List<MetalType> getMetalTypes() {
        return Arrays.stream(MetalType.values())
                .filter(type -> type.getCategory() == this)
                .collect(Collectors.toList());
    }
}
