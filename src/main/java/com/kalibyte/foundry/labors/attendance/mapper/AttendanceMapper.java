package com.kalibyte.foundry.labors.attendance.mapper;

import com.kalibyte.foundry.labors.attendance.dto.AttendanceRequest;
import com.kalibyte.foundry.labors.attendance.dto.AttendanceResponse;
import com.kalibyte.foundry.labors.attendance.entity.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttendanceMapper {
	@Mapping(target = "id", ignore = true)
	Attendance toEntity(AttendanceRequest request);

	@Mapping(target = "laborerId", source = "laborer.id")
	@Mapping(target = "laborerName", source = "laborer.name")
	AttendanceResponse toResponse(Attendance attendance);

	List<AttendanceResponse> toResponseList(List<Attendance> attendances);
}
