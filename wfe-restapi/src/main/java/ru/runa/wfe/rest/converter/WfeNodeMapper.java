package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.runa.wfe.lang.dto.WfNode;
import ru.runa.wfe.rest.dto.WfeNode;

@Mapper(uses = WfeTransitionMapper.class)
public interface WfeNodeMapper {

    @Mapping(source = "arrivingTransitionIds", target = "arrivingTransitions")
    @Mapping(source = "leavingTransitionIds", target = "leavingTransitions")
    WfeNode map(WfNode node);

    List<WfeNode> map(List<WfNode> nodes);
}
