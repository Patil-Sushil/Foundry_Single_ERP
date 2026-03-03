package com.kalibyte.foundry.furnace.furnace_report.dto.Request;

import com.kalibyte.foundry.furnace.furnace_heats.dto.FurnaceHeatRequest;
import com.kalibyte.foundry.furnace.furnace_report.entity.Enum.Shift;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FurnaceRequestDTO {
	@NotBlank
	private String operatorName;
	@NotNull
	private Shift shift;
	@NotBlank
	private String inchargeName;

	private LocalDate date;

	@NotNull
	@Valid
	private List<FurnaceHeatRequest> heats;
}
