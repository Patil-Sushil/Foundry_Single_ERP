package com.kalibyte.foundry.inventory.department.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DepartmentRequest{
	private String name;
	private String code;
}