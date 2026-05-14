package com.kalibyte.foundry.enquiry.mapper;

import com.kalibyte.foundry.enquiry.dto.response.EnquiryItemResponse;
import com.kalibyte.foundry.enquiry.dto.response.EnquiryResponse;
import com.kalibyte.foundry.enquiry.entity.Enquiry;
import com.kalibyte.foundry.enquiry.entity.EnquiryItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Enquiry Mapper
 *
 * PURPOSE:
 * - Convert Entity → Response DTO
 * - Hide internal fields
 */
@Mapper(componentModel = "spring")
public interface EnquiryMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "enquiryItems", target = "items")
    EnquiryResponse toResponse(Enquiry enquiry);

    List<EnquiryResponse> toResponseList(List<Enquiry> enquiries);

    //--------------------------------------------
    // ITEM MAPPING
    //--------------------------------------------

    @Mapping(source = "metalCategory.displayName", target = "metalCategory")
    @Mapping(source = "metalType.displayName", target = "metalType")
    @Mapping(source = "materialGrade", target = "materialGrade")
    @Mapping(source = "castingProcess.id", target = "castingProcessId")
    @Mapping(source = "castingProcess.name", target = "castingProcessName")


    // IMPORTANT FIX
    @Mapping(source = "patternProvidedBy", target = "patternProvidedBy")

    EnquiryItemResponse toItemResponse(EnquiryItem item);

    List<EnquiryItemResponse> toItemResponseList(List<EnquiryItem> items);
}