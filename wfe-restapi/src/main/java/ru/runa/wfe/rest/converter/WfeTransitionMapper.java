package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.lang.dto.WfTransition;
import ru.runa.wfe.rest.dto.WfeTransition;

@Mapper
public abstract class WfeTransitionMapper {

    public abstract WfeTransition map(WfTransition transition);

    public abstract List<WfeTransition> map(List<WfTransition> transitions);

}
