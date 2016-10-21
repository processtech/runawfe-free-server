package ru.runa.wfe.script.permission;

import java.util.List;
import java.util.Set;

import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.user.Executor;

/**
 * Change identifiable permission action type.
 */
public enum ChangePermissionType {
    ADD {

        @Override
        public Set<ru.runa.wfe.security.Permission> updatePermission(ScriptExecutionContext context, Executor executor, Identifiable identifiable,
                Set<ru.runa.wfe.security.Permission> changedPermission) {
            List<ru.runa.wfe.security.Permission> ownPermissions = context.getAuthorizationLogic().getIssuedPermissions(context.getUser(), executor,
                identifiable);
            return ru.runa.wfe.security.Permission.mergePermissions(changedPermission, ownPermissions);
        }
    },
    REMOVE {

        @Override
        public Set<ru.runa.wfe.security.Permission> updatePermission(ScriptExecutionContext context, Executor executor, Identifiable identifiable,
                Set<ru.runa.wfe.security.Permission> changedPermission) {
            List<ru.runa.wfe.security.Permission> ownPermissions = context.getAuthorizationLogic().getIssuedPermissions(context.getUser(), executor,
                identifiable);
            return ru.runa.wfe.security.Permission.subtractPermissions(ownPermissions, changedPermission);
        }
    },
    SET {

        @Override
        public Set<ru.runa.wfe.security.Permission> updatePermission(ScriptExecutionContext context, Executor executor, Identifiable identifiable,
                Set<ru.runa.wfe.security.Permission> changedPermission) {
            return changedPermission;
        }
    };

    public abstract Set<ru.runa.wfe.security.Permission> updatePermission(ScriptExecutionContext context, Executor executor,
            Identifiable identifiable, Set<ru.runa.wfe.security.Permission> changedPermission);
}