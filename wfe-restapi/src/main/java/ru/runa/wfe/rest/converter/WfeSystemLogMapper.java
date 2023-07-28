package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.rest.dto.WfeSystemLog;

@Mapper
public interface WfeSystemLogMapper {
    WfeSystemLog map(SystemLog log);

    List<WfeSystemLog> map(List<SystemLog> logs);
}
