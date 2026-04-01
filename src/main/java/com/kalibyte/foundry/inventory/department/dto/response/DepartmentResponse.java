package com.kalibyte.foundry.inventory.department.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DepartmentResponse{
	Long id;
	String name;
	String code;
}
