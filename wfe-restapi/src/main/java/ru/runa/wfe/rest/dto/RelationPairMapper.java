package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.runa.wfe.relation.RelationPair;
import java.util.List;

@Mapper(uses = WfRelationDtoMapper.class)
public interface RelationPairMapper {

    @Mappings({@Mapping(source = "left.id", target = "leftId"), @Mapping(source = "right.id", target = "rightId")})
    RelationPairDto map(RelationPair pair);

    List<RelationPairDto> map(List<RelationPair> pairs);
}
