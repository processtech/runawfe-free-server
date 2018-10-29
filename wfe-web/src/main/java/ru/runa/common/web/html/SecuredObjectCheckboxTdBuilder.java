package ru.runa.common.web.html;

import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class SecuredObjectCheckboxTdBuilder extends CheckboxTdBuilder {
    public SecuredObjectCheckboxTdBuilder(Permission permission) {
        super(null, permission);
    }

    @Override
    protected String getIdValue(Object object) {
        SecuredObject securedObject = (SecuredObject) object;
        return String.valueOf(securedObject.getIdentifiableId());
    }
}
