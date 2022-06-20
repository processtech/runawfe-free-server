package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.report.dto.WfReportParameter;
import ru.runa.wfe.rest.dto.WfeReportParameter;

@Mapper
public abstract class WfeReportParameterMapper {
    public abstract WfeReportParameter map(WfReportParameter parameter);
    public abstract List<WfeReportParameter> map(List<WfReportParameter> parameters);
}
