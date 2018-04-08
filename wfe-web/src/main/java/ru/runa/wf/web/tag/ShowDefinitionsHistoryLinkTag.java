package ru.runa.wf.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.tag.LinkTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "showDefinitionsHistoryLink")
public class ShowDefinitionsHistoryLinkTag extends LinkTag {

    private static final long serialVersionUID = -1626836525120255263L;

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.DEPLOY_DEFINITION, ASystem.INSTANCE);
    }

    @Override
    protected String getLinkText() {
        return MessagesProcesses.TITLE_DEFINITIONS_HISTORY.message(pageContext);
    }
}
