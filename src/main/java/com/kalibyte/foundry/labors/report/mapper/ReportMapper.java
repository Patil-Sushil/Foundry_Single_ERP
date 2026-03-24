package com.kalibyte.foundry.labors.report.mapper;

import com.kalibyte.foundry.labors.attendance.entity.Attendance;
import com.kalibyte.foundry.labors.report.dto.LaborAttendanceReportDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReportMapper {

    @Mapping(target = "laborerName", source = "laborer.name")
    @Mapping(target = "pieceCompleted", source = "piecesCompleted")
    LaborAttendanceReportDTO toAttendanceReport(Attendance attendance);
}
