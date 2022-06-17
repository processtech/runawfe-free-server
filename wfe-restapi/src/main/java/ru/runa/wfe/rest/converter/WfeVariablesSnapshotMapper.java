package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.rest.dto.WfeVariablesSnapshot;
import ru.runa.wfe.var.dto.WfVariableHistoryState;

@Mapper(uses = WfeVariableMapper.class)
public interface WfeVariablesSnapshotMapper {
    WfeVariablesSnapshot map(WfVariableHistoryState state);

    List<WfeVariablesSnapshot> map(List<WfVariableHistoryState> states);

}
