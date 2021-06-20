package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.var.UserType;
import java.util.List;

@Mapper(uses = VariableDefinitionMapper.class)
public interface UserTypeMapper {
    UserTypeDto map(UserType type);

    List<UserTypeDto> map(List<UserType> types);
}
