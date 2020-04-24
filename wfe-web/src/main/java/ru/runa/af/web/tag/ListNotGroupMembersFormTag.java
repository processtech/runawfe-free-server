package ru.runa.af.web.tag;

import java.util.List;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.AddMembersToGroupAction;
import ru.runa.common.web.MessagesCommon;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listNotGroupMembersForm")
public class ListNotGroupMembersFormTag extends ListExecutorsBaseFormTag {

    private static final long serialVersionUID = 1770247337446619592L;

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_ADD.message(pageContext);
    }

    @Override
    protected List<? extends Executor> getExecutors() {
        Group group = (Group) getExecutor();
        return Delegates.getExecutorService().getGroupChildren(getUser(), group, getBatchPresentation(), true);
    }

    @Override
    protected int getExecutorsCount() {
        Group group = (Group) getExecutor();
        return Delegates.getExecutorService().getGroupChildrenCount(getUser(), group, getBatchPresentation(), true);
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_ADD_EXECUTORS_TO_GROUP.message(pageContext);
    }

    @Override
    public String getAction() {
        return AddMembersToGroupAction.ACTION_PATH;
    }

    @Override
    protected Permission getExecutorsPermission() {
        return Permission.READ;
    }
}
