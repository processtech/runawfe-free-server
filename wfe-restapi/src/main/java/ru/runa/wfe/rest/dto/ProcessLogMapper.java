package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.audit.BaseProcessLog;
import java.util.List;

@Mapper(uses = WfVariableMapper.class)
public interface ProcessLogMapper {
    ProcessLogDto map(BaseProcessLog log);

    List<ProcessLogDto> map(List<BaseProcessLog> log);
}
