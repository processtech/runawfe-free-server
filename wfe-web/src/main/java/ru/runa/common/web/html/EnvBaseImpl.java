package ru.runa.common.web.html;

import java.util.HashMap;
import java.util.Map;
import ru.runa.common.web.Commons;
import ru.runa.common.web.html.TdBuilder.Env;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public abstract class EnvBaseImpl implements Env {
    private User user;

    @Override
    public User getUser() {
        if (user == null) {
            user = Commons.getUser(getPageContext().getSession());
        }
        return user;
    }

    @Override
    public boolean hasProcessDefinitionPermission(Permission permission, Long processDefinitionId) {
        try {
            Boolean result = processDefPermissionCache.get(processDefinitionId);
            if (result != null) {
                return result;
            }
            WfDefinition definition = Delegates.getDefinitionService().getProcessDefinition(getUser(), processDefinitionId);
            result = Delegates.getAuthorizationService().isAllowed(getUser(), permission, definition);
            processDefPermissionCache.put(processDefinitionId, result);
            return result;
        } catch (AuthorizationException e) {
            processDefPermissionCache.put(processDefinitionId, false);
            return false;
        }
    }

    @Override
    public boolean isExcelExport() {
        return false;
    }

    private final Map<Long, Boolean> processDefPermissionCache = new HashMap<>();
}
