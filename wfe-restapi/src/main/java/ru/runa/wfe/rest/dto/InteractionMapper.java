package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.form.Interaction;
import java.util.List;

@Mapper(uses = WfVariableMapper.class)
public interface InteractionMapper {
    InteractionDto map(Interaction interaction);

    List<InteractionDto> map(List<Interaction> interactions);
}
