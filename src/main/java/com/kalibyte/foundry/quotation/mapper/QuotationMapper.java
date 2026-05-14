package com.kalibyte.foundry.quotation.mapper;

import com.kalibyte.foundry.quotation.dto.response.QuotationItemResponse;
import com.kalibyte.foundry.quotation.dto.response.QuotationResponse;
import com.kalibyte.foundry.quotation.entity.Quotation;
import com.kalibyte.foundry.quotation.entity.QuotationItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuotationMapper {

    //--------------------------------------------------
    // MAIN
    //--------------------------------------------------
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "enquiry.id", target = "enquiryId")
    @Mapping(source = "enquiry.enquiryNo", target = "enquiryNumber")
    QuotationResponse toResponse(Quotation quotation);

    List<QuotationResponse> toResponseList(List<Quotation> quotations);

    //--------------------------------------------------
    // ITEM
    //--------------------------------------------------
    @Mapping(source = "metalType.displayName", target = "metalType")
    @Mapping(source = "metalCategory.displayName", target = "metalCategory")
    @Mapping(source = "castingProcess.id", target = "castingProcessId")
    @Mapping(source = "castingProcess.name", target = "castingProcessName")
    @Mapping(source = "isMachiningRequired", target = "isMachiningRequired")
    @Mapping(source = "patternStatus", target = "patternStatus")
    @Mapping(source = "patternProvidedByCustomer", target = "patternProvidedByCustomer")
    @Mapping(target = "receiptName", ignore = true)
    @Mapping(target = "receiptType", ignore = true)
    @Mapping(target = "receiptMaterial", ignore = true)
    @Mapping(target = "inwardDate", ignore = true)
    @Mapping(target = "outwardDate", ignore = true)
    @Mapping(target = "patternNumber", ignore = true)
    @Mapping(target = "patternName", ignore = true)
    @Mapping(target = "patternType", ignore = true)
    QuotationItemResponse toItemResponse(QuotationItem item);

    List<QuotationItemResponse> toItemResponseList(List<QuotationItem> items);

    //--------------------------------------------------
    // AFTER MAPPING
    //--------------------------------------------------
    @AfterMapping
    default void mapExtraDetails(QuotationItem item,
                                 @MappingTarget QuotationItemResponse response) {
        if (item == null) return;

        // Pattern Details
        if (Boolean.TRUE.equals(item.getPatternProvidedByCustomer())) {
            if (item.getPatternReceipt() != null) {
                response.setReceiptName(item.getPatternReceipt().getName());
                response.setReceiptType(String.valueOf(item.getPatternReceipt().getType()));
                if (item.getPatternReceipt().getMaterial() != null) {
                    response.setReceiptMaterial(item.getPatternReceipt().getMaterial().name());
                }
                if (item.getPatternReceipt().getInwardDate() != null) {
                    response.setInwardDate(item.getPatternReceipt().getInwardDate().toString());
                }
                if (item.getPatternReceipt().getOutwardDate() != null) {
                    response.setOutwardDate(item.getPatternReceipt().getOutwardDate().toString());
                }
            }
        } else {
            if (item.getPattern() != null) {
                response.setPatternNumber(item.getPattern().getPatternNumber());
                response.setPatternName(item.getPattern().getPatternName());
                response.setPatternType(String.valueOf(item.getPattern().getType()));
            }
        }
    }
}