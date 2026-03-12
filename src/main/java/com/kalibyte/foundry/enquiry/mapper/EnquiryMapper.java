package com.kalibyte.foundry.enquiry.mapper;

import com.kalibyte.foundry.enquiry.dto.response.EnquiryItemResponse;
import com.kalibyte.foundry.enquiry.dto.response.EnquiryResponse;
import com.kalibyte.foundry.enquiry.entity.Enquiry;
import com.kalibyte.foundry.enquiry.entity.EnquiryItem;
import com.kalibyte.foundry.pattern.entity.Pattern;
import com.kalibyte.foundry.pattern.entity.PatternReceipt;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EnquiryMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "enquiryItems", target = "items")
    EnquiryResponse toResponse(Enquiry enquiry);

    List<EnquiryResponse> toResponseList(List<Enquiry> enquiries);

    @Mapping(source = "metalCategory.displayName", target = "metalCategory")
    @Mapping(source = "metalType.displayName", target = "metalType")
    @Mapping(target = "patternName", expression = "java(getPatternName(item))")
    @Mapping(target = "patternType", expression = "java(getPatternType(item))")
    @Mapping(target = "patternMaterial", expression = "java(getPatternMaterial(item))")
    @Mapping(target = "inwardDate", expression = "java(getInwardDate(item))")
    @Mapping(target = "outwardDate", expression = "java(getOutwardDate(item))")
    EnquiryItemResponse toItemResponse(EnquiryItem item);

    List<EnquiryItemResponse> toItemResponseList(List<EnquiryItem> items);

    // ---------- Custom Logic ----------

    default String getPatternName(EnquiryItem item) {
        if (Boolean.TRUE.equals(item.getPatternProvidedByCustomer())) {
            PatternReceipt pr = item.getPatternReceipt();
            return pr != null ? pr.getName() : null;
        } else {
            Pattern pattern = item.getPattern();
            return pattern != null ? pattern.getName() : null;
        }
    }

    default String getPatternType(EnquiryItem item) {
        if (Boolean.TRUE.equals(item.getPatternProvidedByCustomer())) {
            PatternReceipt pr = item.getPatternReceipt();
            return pr != null ? pr.getType().name() : null;
        } else {
            Pattern pattern = item.getPattern();
            return pattern != null ? pattern.getType().name() : null;
        }
    }

    default String getPatternMaterial(EnquiryItem item) {
        if (Boolean.TRUE.equals(item.getPatternProvidedByCustomer())) {
            PatternReceipt pr = item.getPatternReceipt();
            return pr != null ? pr.getMaterial().name() : null;
        } else {
            Pattern pattern = item.getPattern();
            return pattern != null ? pattern.getMaterial().name() : null;
        }
    }

    default java.time.LocalDate getInwardDate(EnquiryItem item) {
        if (Boolean.TRUE.equals(item.getPatternProvidedByCustomer())) {
            PatternReceipt pr = item.getPatternReceipt();
            return pr != null ? pr.getInwardDate() : null;
        }
        return null;
    }

    default java.time.LocalDate getOutwardDate(EnquiryItem item) {
        if (Boolean.TRUE.equals(item.getPatternProvidedByCustomer())) {
            PatternReceipt pr = item.getPatternReceipt();
            return pr != null ? pr.getOutwardDate() : null;
        }
        return null;
    }
}