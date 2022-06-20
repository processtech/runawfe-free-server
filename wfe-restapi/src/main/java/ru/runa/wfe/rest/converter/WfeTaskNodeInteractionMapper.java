package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.rest.dto.WfeTaskNodeInteraction;

@Mapper(uses = { WfeTransitionMapper.class, WfeVariableDefinitionMapper.class })
public interface WfeTaskNodeInteractionMapper {

    @Mapping(source = "variables", target = "variableDefinitions")
    WfeTaskNodeInteraction map(Interaction interaction);

    List<WfeTaskNodeInteraction> map(List<Interaction> interactions);
}
