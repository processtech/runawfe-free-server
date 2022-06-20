package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.rest.dto.WfeProcessDefinition;

@Mapper(uses = WfeUserMapper.class)
public interface WfeProcessDefinitionMapper {

    @Mapping(source = "versionId", target = "id")
    @Mapping(source = "createActor", target = "createUser")
    @Mapping(source = "updateActor", target = "updateUser")
    WfeProcessDefinition map(WfDefinition definition);

    List<WfeProcessDefinition> map(List<WfDefinition> definitions);
}
