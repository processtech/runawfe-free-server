package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.runa.wfe.rest.dto.WfeExecutor;
import ru.runa.wfe.rest.dto.WfeGroup;
import ru.runa.wfe.rest.dto.WfeUser;
import ru.runa.wfe.user.DelegationGroup;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;

@Mapper
public interface WfeGroupMapper {

    WfeGroup map(Group group);

    List<WfeGroup> map(List<Group> groups);

    @AfterMapping
    public default void additionalProperties(Group element, @MappingTarget WfeUser target) {
        if (element instanceof DelegationGroup) {
            target.setType(WfeExecutor.Type.USER);
        } else if (element instanceof EscalationGroup) {
            target.setType(WfeExecutor.Type.ESCALATION_GROUP);
        } else if (element instanceof TemporaryGroup) {
            target.setType(WfeExecutor.Type.TEMPORARY_GROUP);
        } else {
            target.setType(WfeExecutor.Type.GROUP);
        }
    }

    default Group map(WfeGroup dto) {
        Group group = new Group(null, null);
        fill(group, dto);
        return group;
    }

    default void fill(Group group, WfeGroup dto) {
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setFullName(dto.getFullName());
    }
}
