package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.rest.dto.WfeReport;

@Mapper
public interface WfeReportMapper {
    WfeReport map(WfReport report);
    List<WfeReport> map(List<WfReport> reports);
}
