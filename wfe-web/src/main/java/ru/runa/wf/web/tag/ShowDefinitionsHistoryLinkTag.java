package ru.runa.wf.web.tag;

import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.wfe.definition.WorkflowSystemPermission;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @jsp.tag name = "showDefinitionsHistoryLink" body-content = "empty"
 */
public class ShowDefinitionsHistoryLinkTag extends LinkTag {

    private static final long serialVersionUID = -1626836525120255263L;

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), WorkflowSystemPermission.DEPLOY_DEFINITION, ASystem.INSTANCE);
    }

    @Override
    protected String getLinkText() {
        return Messages.getMessage(Messages.TITLE_DEFINITIONS_HISTORY, pageContext);
    }
}
