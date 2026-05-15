package com.kalibyte.foundry.inventory.ledger.mapper;

import com.kalibyte.foundry.inventory.ledger.dto.response.VendorLedgerResponse;
import com.kalibyte.foundry.inventory.ledger.entity.VendorLedger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.kalibyte.foundry.inventory.ledger.dto.response.VendorBalanceResponse;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import java.math.BigDecimal;

/**
 * MapStruct mapper for VendorLedger entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VendorLedgerMapper {

    @Mapping(source = "vendor.id", target = "vendorId")
    @Mapping(source = "vendor.name", target = "vendorName")
    @Mapping(source = "materialInward.inwardNumber", target = "inwardNumber")
    VendorLedgerResponse toResponse(VendorLedger ledger);

    @Mapping(source = "vendor.id", target = "vendorId")
    @Mapping(source = "vendor.name", target = "vendorName")
    @Mapping(source = "totalCredit", target = "totalCredit")
    @Mapping(source = "totalDebit", target = "totalDebit")
    @Mapping(source = "outstandingBalance", target = "outstandingBalance")
    VendorBalanceResponse toBalanceResponse(Vendor vendor, BigDecimal totalCredit, BigDecimal totalDebit, BigDecimal outstandingBalance);
}
