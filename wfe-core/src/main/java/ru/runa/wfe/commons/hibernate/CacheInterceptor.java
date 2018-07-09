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
package ru.runa.wfe.commons.hibernate;

import java.io.Serializable;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DbType;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.commons.cache.Change;

public class CacheInterceptor extends EmptyInterceptor {
    private static final long serialVersionUID = 1L;

    private boolean isOracleDatabase() {
        return ApplicationContextFactory.getDBType() == DbType.ORACLE;
    }

    private boolean onChanges(Object entity, Change change, Object[] state, Object[] previousState, String[] propertyNames, Type[] types,
            boolean fixOracleStrings) {
        boolean modified = false;
        if (fixOracleStrings && isOracleDatabase()) {
            // Oracle handles empty strings as NULLs so we change empty strings to ' '.
            for (int i = 0; i < state.length; ++i) {
                if (state[i] instanceof String && ((String) state[i]).length() == 0) {
                    state[i] = " ";
                    modified = true;
                }
            }
        }
        if (SystemProperties.useCacheStateMachine()) {
            ru.runa.wfe.commons.cache.sm.CachingLogic.onChange(entity, change, state, previousState, propertyNames, types);
        } else {
            CachingLogic.onChange(entity, change, state, previousState, propertyNames, types);
        }
        return modified;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        return onChanges(entity, Change.CREATE, state, null, propertyNames, types, true);
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        onChanges(entity, Change.DELETE, state, null, propertyNames, types, false);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] state, Object[] previousState, String[] propertyNames, Type[] types) {
        return onChanges(entity, Change.UPDATE, state, previousState, propertyNames, types, true);
    }
}
