package ru.runa.wfe.var.logic;

import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;

public interface InternalStorageReferenceService {

    String ID_ATTRIBUTE_NAME = "id";

    UserTypeMap loadById(UserType userType, Long id);

    long insert(UserType userType, UserTypeMap value);

    void update(UserType userType, Long id, UserTypeMap value);

    void delete(UserType userType, Long id);

}
