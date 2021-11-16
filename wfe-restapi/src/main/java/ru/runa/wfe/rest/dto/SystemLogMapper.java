package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.audit.SystemLog;
import java.util.List;

@Mapper(uses = WfVariableMapper.class)
public interface SystemLogMapper {
    SystemLogDto map(SystemLog log);

    List<SystemLogDto> map(List<SystemLog> logs);
}
