package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.tag.IdLinkBaseTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

/**
 * Link to executor's tasks list form
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "listExecutorTasksLink")
public class ListExecutorTasksLinkTag extends IdLinkBaseTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean isLinkEnabled() {
        return getUser().getActor().getId().equals(getIdentifiableId())
                || Delegates.getAuthorizationService().isAllowed(getUser(), Permission.VIEW_TASKS, SecuredObjectType.ACTOR, getIdentifiableId())
                || Delegates.getAuthorizationService().isAllowed(getUser(), Permission.VIEW_TASKS, SecuredObjectType.GROUP, getIdentifiableId());
    }

    @Override
    protected String getLinkText() {
        Executor executor = Delegates.getExecutorService().getExecutor(getUser(), getIdentifiableId());
        if (executor.getClass().getSimpleName().equals("Group")) {
            return MessagesProcesses.TITLE_GROUP_TASKS.message(pageContext);
        }
        return MessagesProcesses.TITLE_ACTOR_TASKS.message(pageContext);
    }
}
