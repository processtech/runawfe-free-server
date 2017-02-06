package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.tag.IdLinkBaseTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.ActorPermission;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.GroupPermission;

/**
 * Link to executor's tasks list form
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "listExecutorTasksLink")
public class ListExecutorTasksLinkTag extends IdLinkBaseTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), ActorPermission.READ_USER_TASKS, SecuredObjectType.ACTOR, getIdentifiableId())
                || Delegates.getAuthorizationService().isAllowed(getUser(), GroupPermission.READ_GROUPUSERS_TASKS, SecuredObjectType.GROUP, getIdentifiableId());
    }



    @Override
    protected String getLinkText() {
        Executor executor = Delegates.getExecutorService().getExecutor(getUser(), getIdentifiableId());
        if (executor.getClass().getSimpleName().equals("Group")) {
            return MessagesProcesses.LABEL_SHOW_GROUP_TASKS.message(pageContext);
        }
        return MessagesProcesses.LABEL_SHOW_EXECUTOR_TASKS.message(pageContext);
    }
}
