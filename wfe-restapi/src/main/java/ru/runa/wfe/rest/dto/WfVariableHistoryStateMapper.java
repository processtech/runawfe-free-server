package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.var.dto.WfVariableHistoryState;
import java.util.List;

@Mapper(uses = WfVariableMapper.class)
public interface WfVariableHistoryStateMapper {
    WfVariableHistoryStateDto map(WfVariableHistoryState state);

    List<WfVariableHistoryStateDto> map(List<WfVariableHistoryState> states);
}
