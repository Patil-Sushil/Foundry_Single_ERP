package com.kalibyte.foundry.labors.report.mapper;

import com.kalibyte.foundry.labors.attendance.entity.Attendance;
import com.kalibyte.foundry.labors.report.dto.LaborAttendanceReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReportMapper {

    @Mapping(target = "pieceCompleted", source = "piecesCompleted")
    LaborAttendanceReport toAttendanceReport(Attendance attendance);
}
