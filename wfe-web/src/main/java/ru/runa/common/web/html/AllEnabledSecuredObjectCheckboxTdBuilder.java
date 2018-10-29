package ru.runa.common.web.html;

import ru.runa.wfe.security.SecuredObject;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class AllEnabledSecuredObjectCheckboxTdBuilder extends CheckboxTdBuilder {

    public AllEnabledSecuredObjectCheckboxTdBuilder() {
        super(null, null);
    }

    @Override
    protected String getIdValue(Object object) {
        SecuredObject securedObject = (SecuredObject) object;
        return String.valueOf(securedObject.getIdentifiableId());
    }

    @Override
    protected boolean isEnabled(Object object, Env env) {
        return true;
    }
}
