package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.report.dto.WfReport;

import java.util.List;

@Mapper(uses = WfVariableMapper.class)
public interface WfReportDtoMapper {
    WfReportDto map(WfReport report);
    List<WfReportDto> map(List<WfReport> reports);
}
