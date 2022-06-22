package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.execution.dto.WfToken;
import ru.runa.wfe.rest.dto.WfeToken;

@Mapper(uses = WfeNodeMapper.class)
public interface WfeTokenMapper {
    WfeToken map(WfToken token);

    List<WfeToken> map(List<WfToken> tokens);
}
