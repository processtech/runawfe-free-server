package ru.runa.wfe.commons.hibernate;

import ru.runa.wfe.security.SecuredObjectType;

public class SecuredObjectTypeType extends ExtensibleEnumType {

    @Override
    public Class returnedClass() {
        return SecuredObjectType.class;
    }

    @Override
    public String toXMLString(Object value) {
        return ((SecuredObjectType)value).getName();
    }

    @Override
    public Object fromXMLString(String xmlValue) {
        return SecuredObjectType.valueOf(xmlValue);
    }
}
