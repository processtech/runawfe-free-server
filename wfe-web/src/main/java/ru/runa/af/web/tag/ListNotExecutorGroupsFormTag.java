package ru.runa.af.web.tag;

import java.util.List;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.AddExecutorToGroupsAction;
import ru.runa.common.web.MessagesCommon;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listNotExecutorGroupsForm")
public class ListNotExecutorGroupsFormTag extends ListExecutorsBaseFormTag {

    private static final long serialVersionUID = 5067294728960890661L;

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_ADD.message(pageContext);
    }

    @Override
    protected List<? extends Executor> getExecutors() {
        return Delegates.getExecutorService().getExecutorGroups(getUser(), getExecutor(), getBatchPresentation(), true);
    }

    @Override
    protected int getExecutorsCount() {
        return Delegates.getExecutorService().getExecutorGroupsCount(getUser(), getExecutor(), getBatchPresentation(), true);
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_ADD_EXECUTOR_TO_GROUP.message(pageContext);
    }

    @Override
    public String getAction() {
        return AddExecutorToGroupsAction.ACTION_PATH;
    }

    @Override
    protected Permission getExecutorsPermission() {
        return Permission.UPDATE;  // TODO Was ADD_TO_GROUP. Why this is in *List*ExecutorGroupsFormTag?
    }
}
