package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.relation.Relation;

import java.util.List;

@Mapper(uses = WfVariableMapper.class)
public interface WfRelationDtoMapper {

    WfRelationDto map(Relation relation);

    List<WfRelationDto> map(List<Relation> relations);
}
