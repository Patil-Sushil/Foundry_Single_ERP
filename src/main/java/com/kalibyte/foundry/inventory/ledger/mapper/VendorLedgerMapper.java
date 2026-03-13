package com.kalibyte.foundry.inventory.ledger.mapper;

import com.kalibyte.foundry.inventory.ledger.dto.response.VendorLedgerResponse;
import com.kalibyte.foundry.inventory.ledger.entity.VendorLedger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for VendorLedger entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VendorLedgerMapper {

    @Mapping(source = "vendor.id", target = "vendorId")
    @Mapping(source = "vendor.name", target = "vendorName")
    @Mapping(source = "materialInward.inwardNumber", target = "inwardNumber")
    VendorLedgerResponse toResponse(VendorLedger ledger);
}
