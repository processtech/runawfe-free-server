package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.execution.dto.WfToken;
import java.util.List;

@Mapper(uses = WfNodeMapper.class)
public interface WfTokenMapper {
    WfTokenDto map(WfToken token);

    List<WfTokenDto> map(List<WfToken> tokens);
}
