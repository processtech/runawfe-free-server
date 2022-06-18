package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.rest.dto.WfeProcessLog;

@Mapper
public interface WfeProcessLogMapper {
    WfeProcessLog map(BaseProcessLog log);

    List<WfeProcessLog> map(List<BaseProcessLog> logs);
}
