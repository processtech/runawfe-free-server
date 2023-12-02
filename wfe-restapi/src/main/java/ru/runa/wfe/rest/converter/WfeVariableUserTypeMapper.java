package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.rest.dto.WfeVariableUserType;
import ru.runa.wfe.var.UserType;

@Mapper(uses = WfeVariableDefinitionMapper.class)
public interface WfeVariableUserTypeMapper {
    WfeVariableUserType map(UserType type);

    List<WfeVariableUserType> map(List<UserType> types);
}
