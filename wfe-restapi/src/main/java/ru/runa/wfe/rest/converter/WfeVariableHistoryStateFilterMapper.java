package ru.runa.wfe.rest.converter;

import org.mapstruct.Mapper;
import ru.runa.wfe.audit.VariableHistoryStateFilter;
import ru.runa.wfe.rest.dto.WfeVariableHistoryStateFilter;

@Mapper
public interface WfeVariableHistoryStateFilterMapper {

    VariableHistoryStateFilter map(WfeVariableHistoryStateFilter dto);

}
