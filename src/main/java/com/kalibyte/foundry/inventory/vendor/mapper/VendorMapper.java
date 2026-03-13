package com.kalibyte.foundry.inventory.vendor.mapper;

import com.kalibyte.foundry.inventory.vendor.dto.request.CreateVendorRequest;
import com.kalibyte.foundry.inventory.vendor.dto.request.UpdateVendorRequest;
import com.kalibyte.foundry.inventory.vendor.dto.response.VendorResponse;
import com.kalibyte.foundry.inventory.vendor.dto.response.VendorSummary;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VendorMapper {
    VendorResponse toResponse(Vendor vendor);
    VendorSummary toSummary(Vendor vendor);
    Vendor toEntity(CreateVendorRequest request);
    void updateEntity(UpdateVendorRequest request, @MappingTarget Vendor vendor);
}
