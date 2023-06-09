package ru.runa.wfe.commons.hibernate;

import java.io.Serializable;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import ru.runa.wfe.audit.ArchivedProcessLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DbType;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.sm.CachingLogic;
import ru.runa.wfe.execution.ArchivedNodeProcess;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.execution.ArchivedSwimlane;
import ru.runa.wfe.execution.ArchivedToken;
import ru.runa.wfe.var.ArchivedVariable;

public class WfeInterceptor extends EmptyInterceptor {
    private static final long serialVersionUID = 1L;

    private boolean isOracleDatabase() {
        return ApplicationContextFactory.getDbType() == DbType.ORACLE;
    }

    private boolean onChanges(Object entity, Change change, Object[] state, Object[] previousState, String[] propertyNames, boolean fixOracleStrings) {
        // Deleting a process automatically entails tokens update. Ignore it.
        if (change == Change.UPDATE && entity instanceof ArchivedToken) {
            return false;
        }

        // Archive immutability support:
        // NOTE: This check is mandatory, because some archived entity classes HAVE public setters.
        //       E.g. variables, since VariableLogic.getProcessStateOnTime() creates temporary fake variables which are then proxied.
        //       Also, a reflection potentially can be used to make archived variable dirty.
        if (change != Change.DELETE && (entity instanceof ArchivedNodeProcess ||
                entity instanceof ArchivedProcess ||
                entity instanceof ArchivedProcessLog ||
                entity instanceof ArchivedSwimlane ||
                entity instanceof ArchivedToken ||
                entity instanceof ArchivedVariable
        )) {
            throw new RuntimeException("Attempted to " + change + " immutable " + entity);
        }

        // Cache invalidatioin support:
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
        CachingLogic.onChange(entity, change, state, previousState, propertyNames);
        return modified;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        return onChanges(entity, Change.CREATE, state, null, propertyNames, true);
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        onChanges(entity, Change.DELETE, state, null, propertyNames, false);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] state, Object[] previousState, String[] propertyNames, Type[] types) {
        return onChanges(entity, Change.UPDATE, state, previousState, propertyNames, true);
    }
}
