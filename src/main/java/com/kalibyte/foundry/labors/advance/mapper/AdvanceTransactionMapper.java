package com.kalibyte.foundry.labors.advance.mapper;

import com.kalibyte.foundry.labors.advance.dto.AdvanceTransactionRequest;
import com.kalibyte.foundry.labors.advance.dto.AdvanceTransactionResponse;
import com.kalibyte.foundry.labors.advance.entity.AdvanceTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdvanceTransactionMapper {
	AdvanceTransaction toEntity(AdvanceTransactionRequest requestDTO);

	AdvanceTransactionResponse toResponseDTO(AdvanceTransaction entity);
}
