package ru.runa.wf.web.tag;

import ru.runa.common.web.tag.SecuredObjectFormTag;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 30.08.2004
 * 
 */
public abstract class ProcessDefinitionBaseFormTag extends SecuredObjectFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected SecuredObject getSecuredObject() {
        return getDefinition();
    }

    protected WfDefinition getDefinition() {
        return Delegates.getDefinitionService().getProcessDefinition(getUser(), getIdentifiableId());
    }

    @Override
    protected boolean isVisible() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.READ, getDefinition());
    }
}
