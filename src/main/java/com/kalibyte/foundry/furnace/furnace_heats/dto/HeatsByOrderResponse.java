package com.kalibyte.foundry.furnace.furnace_heats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeatsByOrderResponse {
	private UUID orderId;
	private String orderNumber;
	private List<FurnaceHeatResponse> heats;
}
