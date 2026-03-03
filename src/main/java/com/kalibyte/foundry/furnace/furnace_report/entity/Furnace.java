package com.kalibyte.foundry.furnace.furnace_report.entity;

import com.kalibyte.foundry.furnace.furnace_heats.entity.FurnaceHeats;
import com.kalibyte.foundry.furnace.furnace_report.entity.Enum.Shift;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "furnace_reports")
@NamedQueries({
		@NamedQuery(name = "Furnace.findByHeatsNotEmpty", query = "select f from Furnace f where f.heats is not empty")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Furnace {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String furnaceRefNo;

	private String operatorName;

	@Enumerated(EnumType.STRING)
	private Shift shift;

	private String inchargeName;

	private LocalDate date;

	@OneToMany(mappedBy = "furnace", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FurnaceHeats> heats = new java.util.ArrayList<>();

	public void addHeat(FurnaceHeats heat) {
		if (heats == null) {
			heats = new java.util.ArrayList<>();
		}
		heats.add(heat);
		heat.setFurnace(this);
	}

	public void removeHeat(FurnaceHeats heat) {
		if (heats != null) {
			heats.remove(heat);
			heat.setFurnace(null);
		}
	}
}
