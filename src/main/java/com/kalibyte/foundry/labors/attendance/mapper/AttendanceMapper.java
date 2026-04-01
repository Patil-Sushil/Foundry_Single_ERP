package com.kalibyte.foundry.labors.attendance.mapper;

import com.kalibyte.foundry.labors.attendance.dto.AttendanceRequestDTO;
import com.kalibyte.foundry.labors.attendance.dto.AttendanceResponseDTO;
import com.kalibyte.foundry.labors.attendance.entity.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttendanceMapper {
	@Mapping(target = "id", ignore = true)
	Attendance toEntity(AttendanceRequestDTO attendanceRequestDTO);

	AttendanceRequestDTO toDTO(Attendance attendance);

	AttendanceResponseDTO toResponse(Attendance attendance);

	List<AttendanceResponseDTO> toResponseDTOList(List<Attendance> attendances);

	List<AttendanceResponseDTO> toResponseDTOList1(List<AttendanceResponseDTO> attendances);

}
