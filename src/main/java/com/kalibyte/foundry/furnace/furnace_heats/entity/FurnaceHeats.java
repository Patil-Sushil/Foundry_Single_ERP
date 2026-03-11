package com.kalibyte.foundry.furnace.furnace_heats.entity;

import com.kalibyte.foundry.furnace.furnace_report.entity.Furnace;
import com.kalibyte.foundry.order.entity.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="furnace_heats")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FurnaceHeats {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private double sipercentage;

	private double cpcpercentage;

	private double mgpercentage;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "furnace_id")
	private Furnace furnace;

	private double startReading;

	private double stopReading;

	private double differenceReading;

	private double totalWeight;

	private double pouringTemp;

	private double powerToWeight;

	@OneToMany(mappedBy = "heat", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<HeatMaterialItem> materialsUsed;

	private LocalTime pouringStartTime;

	private LocalTime pouringEndTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	public void addMaterial(HeatMaterialItem material) {
		if (materialsUsed == null) {
			materialsUsed = new ArrayList<>();
		}
		materialsUsed.add(material);
		material.setHeat(this);
	}

	public void removeMaterial(HeatMaterialItem material) {
		if (materialsUsed != null) {
			materialsUsed.remove(material);
			material.setHeat(null);
		}
	}
}
