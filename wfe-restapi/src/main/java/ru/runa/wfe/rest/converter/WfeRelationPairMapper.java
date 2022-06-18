package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.rest.dto.WfeRelationPair;

@Mapper(uses = WfeRelationMapper.class)
public interface WfeRelationPairMapper {

    @Mappings({@Mapping(source = "left.id", target = "leftId"), @Mapping(source = "right.id", target = "rightId")})
    WfeRelationPair map(RelationPair pair);

    List<WfeRelationPair> map(List<RelationPair> pairs);
}
