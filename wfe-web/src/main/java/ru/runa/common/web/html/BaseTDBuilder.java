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

import org.apache.commons.beanutils.BeanUtils;

import ru.runa.common.web.html.TDBuilder.Env.IdentifiableExtractor;
import ru.runa.wfe.security.Permission;

import com.google.common.base.Throwables;

/**
 * Created 07.07.2005
 *
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public abstract class BaseTDBuilder implements TDBuilder {
    private final Permission permission;
    private IdentifiableExtractor identifiableExtractor;

    public BaseTDBuilder(Permission permission) {
        this.permission = permission;
    }

    public BaseTDBuilder(Permission permission, IdentifiableExtractor identifiableExtractor) {
        this(permission);
        this.identifiableExtractor = identifiableExtractor;
    }

    public void setIdentifiableExtractor(IdentifiableExtractor identifiableExtractor) {
        this.identifiableExtractor = identifiableExtractor;
    }

    protected boolean isEnabled(Object object, Env env) {
        if (permission == null) {
            return false;
        }
        return env.isAllowed(permission, identifiableExtractor);
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

    protected IdentifiableExtractor getExtractor() {
        return identifiableExtractor;
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
