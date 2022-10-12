package ru.runa.wfe.rest.converter;

import org.mapstruct.Mapper;
import ru.runa.wfe.report.impl.ReportBuildResult;
import ru.runa.wfe.rest.dto.WfeReportResult;

@Mapper
public interface WfeReportResultMapper {

    WfeReportResult map(ReportBuildResult process);

}
