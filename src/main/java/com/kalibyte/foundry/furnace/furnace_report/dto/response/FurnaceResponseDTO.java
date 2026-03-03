package com.kalibyte.foundry.furnace.furnace_report.dto.response;

import com.kalibyte.foundry.furnace.furnace_heats.dto.FurnaceHeatResponse;
import com.kalibyte.foundry.furnace.furnace_report.entity.Enum.Shift;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FurnaceResponseDTO {
	private Long id;
	private String furnaceRefNo;
	private String operatorName;
	private Shift shift;
	private String inchargeName;
	private LocalDate date;
	private List<FurnaceHeatResponse> heats;
}

