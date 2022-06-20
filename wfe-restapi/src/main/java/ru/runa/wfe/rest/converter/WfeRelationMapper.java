package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.rest.dto.WfeRelation;

@Mapper
public interface WfeRelationMapper {

    WfeRelation map(Relation relation);

    List<WfeRelation> map(List<Relation> relations);

    Relation map(WfeRelation relation);

}
