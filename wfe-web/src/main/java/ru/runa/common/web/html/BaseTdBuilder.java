package ru.runa.common.web.html;

import com.google.common.base.Throwables;
import org.apache.commons.beanutils.BeanUtils;
import ru.runa.common.web.html.TdBuilder.Env.SecuredObjectExtractor;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.Permission;

/**
 * Created 07.07.2005
 * 
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public abstract class BaseTdBuilder implements TdBuilder {
    private Permission permission;
    private SecuredObjectExtractor securedObjectExtractor;

    public BaseTdBuilder(Permission permission) {
        this.permission = permission;
    }

    public BaseTdBuilder(Permission permission, SecuredObjectExtractor securedObjectExtractor) {
        this(permission);
        this.securedObjectExtractor = securedObjectExtractor;
    }

    public void setSecuredObjectExtractor(SecuredObjectExtractor securedObjectExtractor) {
        this.securedObjectExtractor = securedObjectExtractor;
    }

    protected boolean isEnabled(Object object, Env env) {
        return isEnabledFor(object, env, permission);
    }
    
    protected boolean isEnabledFor(Object object, Env env, Permission perm) {
        if (perm == null) {
            return false;
        }
        if (perm == Permission.START_PROCESS) {
            return ((WfDefinition) object).isCanBeStarted();
        }
        return env.isAllowed(perm, securedObjectExtractor);
    }

    protected String readProperty(Object object, String propertyName, boolean isExceptionOnAbsent) {
        try {
            return BeanUtils.getNestedProperty(object, propertyName);
        } catch (NoSuchMethodException e) {
            if (isExceptionOnAbsent) {
                throw Throwables.propagate(e);
            }
            return "";
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    protected SecuredObjectExtractor getExtractor() {
        return securedObjectExtractor;
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
    
    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
    
}
