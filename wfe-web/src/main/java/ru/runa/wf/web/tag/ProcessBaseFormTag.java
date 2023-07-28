package ru.runa.wf.web.tag;

import ru.runa.common.web.tag.SecuredObjectFormTag;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;

public abstract class ProcessBaseFormTag extends SecuredObjectFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected SecuredObject getSecuredObject() {
        return getProcess();
    }

    protected WfProcess getProcess() {
        return Delegates.getExecutionService().getProcess(getUser(), getIdentifiableId());
    }

    @Override
    protected boolean isVisible() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.READ, getProcess());
    }
}
