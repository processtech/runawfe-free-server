package ru.runa.wfe.rest.converter;

import org.mapstruct.Mapper;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.rest.dto.WfeProcessLogFilter;

@Mapper
public interface WfeProcessLogFilterMapper {

    ProcessLogFilter map(WfeProcessLogFilter dto);

}
