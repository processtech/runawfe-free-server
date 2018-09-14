package ru.runa.wfe.extension.handler.process;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.handler.ParamBasedHandlerActionHandler;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.Executor;

public class AddReadProcessPermissionsHandler extends ParamBasedHandlerActionHandler {
    @Autowired
    private PermissionDao permissionDAO;

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        Object executorsParam = paramsDef.getInputParamValue("executors", executionContext.getVariableProvider());
        List<Executor> executors = TypeConversionUtil.convertTo(List.class, executorsParam);
        if (executors == null) {
            log.warn("Null executors in " + this + ", returning");
            return;
        }
        ru.runa.wfe.execution.Process securedObject = executionContext.getProcess();
        Permission permission = Permission.READ;
        for (Executor executor : executors) {
            List<Permission> permissions = permissionDAO.getIssuedPermissions(executor, securedObject);
            if (!permissions.contains(permission)) {
                permissions.add(permission);
                log.debug("Adding " + permission + " to " + executor + " on " + securedObject);
                permissionDAO.setPermissions(executor, permissions, securedObject);
            } else {
                log.debug(executor + " already contains " + permission + " on " + securedObject);
            }
        }
    }

}
