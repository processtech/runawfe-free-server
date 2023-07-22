package ru.runa.wfe.commons.hibernate;

import ru.runa.wfe.security.Permission;

public class PermissionType extends ExtensibleEnumType {

    @Override
    public Class returnedClass() {
        return Permission.class;
    }

    @Override
    public String toXMLString(Object value) {
        return ((Permission)value).getName();
    }

    @Override
    public Object fromXMLString(String xmlValue) {
        return Permission.valueOf(xmlValue);
    }
}
