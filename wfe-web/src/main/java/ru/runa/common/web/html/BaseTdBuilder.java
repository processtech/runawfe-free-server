/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
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
    private final Permission permission;
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
            return BeanUtils.getProperty(object, propertyName);
        } catch (NoSuchMethodException e) {
            if (isExceptionOnAbsent) {
                throw Throwables.propagate(e);
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return "";
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
}
