package ru.runa.wfe.extension.handler.process;

import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.Executor;

public class ModifyProcessPermissionsHandler extends CommonParamBasedHandler {
    @Autowired
    private PermissionDao permissionDao;
    private final Permission[] processPermissions = {
            Permission.READ,
            Permission.UPDATE_PERMISSIONS,
            Permission.CANCEL,
            Permission.UPDATE,
            Permission.DELETE,
    };

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        Executor executor = handlerData.getInputParamValueNotNull(Executor.class, "executor");
        CurrentProcess securedObject = handlerData.getExecutionContext().getCurrentProcess();
        boolean[] permissionsStatus = {
                handlerData.getInputParamValueNotNull(boolean.class, "read"),
                handlerData.getInputParamValueNotNull(boolean.class, "update_permissions"),
                handlerData.getInputParamValueNotNull(boolean.class, "cancel"),
                handlerData.getInputParamValueNotNull(boolean.class, "update"),
                handlerData.getInputParamValueNotNull(boolean.class, "delete"),
        };
        Set<Permission> toAdd = new HashSet<>();
        for (int i = 0; i < permissionsStatus.length; i++) {
            if (permissionsStatus[i]) {
                toAdd.add(processPermissions[i]);
            }
        }
        permissionDao.setPermissions(executor, toAdd, securedObject);
    }
}
