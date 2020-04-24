package ru.runa.af.web.tag;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.UpdateStatusAction;
import ru.runa.af.web.html.StatusTableBuilder;
import ru.runa.common.web.MessagesCommon;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "updateStatusForm")
public class UpdateStatusFormTag extends UpdateExecutorBaseFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    public void fillFormData(TD formTd) {
        StatusTableBuilder builder = new StatusTableBuilder((Actor) getExecutor(), !isSubmitButtonEnabled(), pageContext);
        formTd.addElement(builder.build());
    }

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_APPLY.message(pageContext);
    }

    @Override
    protected boolean isVisible() {
        return getExecutor() instanceof Actor;
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE_ACTOR_STATUS, getExecutor());
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_ACTOR_STATUS.message(pageContext);
    }

    @Override
    public String getAction() {
        return UpdateStatusAction.ACTION_PATH;
    }
}
