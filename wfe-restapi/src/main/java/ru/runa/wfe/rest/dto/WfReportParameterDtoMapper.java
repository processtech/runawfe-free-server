package ru.runa.wfe.rest.dto;

import java.util.List;
import org.mapstruct.Mapper;

import ru.runa.wfe.report.dto.WfReportParameter;

@Mapper(componentModel = "spring")
public abstract class WfReportParameterDtoMapper {
    public abstract WfReportParameterDto map(WfReportParameter parameter);
    public abstract List<WfReportParameterDto> map(List<WfReportParameter> parameters);
}
