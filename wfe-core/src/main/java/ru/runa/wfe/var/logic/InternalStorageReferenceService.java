package ru.runa.wfe.var.logic;

import java.util.List;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableProvider;

public interface InternalStorageReferenceService {

    String ID_ATTRIBUTE_NAME = "id";

    String BY_REFERENCE_FILE_SUFFIX = "&";

    UserTypeMap loadById(UserType userType, Long id);

    long insert(UserType userType, UserTypeMap value);

    void update(UserType userType, Long id, UserTypeMap value);

    void delete(UserType userType, Long id);

    List<UserTypeMap> findByFilter(UserType userType, String condition, VariableProvider variableProvider);

}
