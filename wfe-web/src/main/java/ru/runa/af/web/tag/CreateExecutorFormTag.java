package ru.runa.af.web.tag;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.CreateExecutorAction;
import ru.runa.af.web.form.UpdateExecutorDetailsForm;
import ru.runa.af.web.html.ExecutorTableBuilder;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "createExecutorForm")
public class CreateExecutorFormTag extends TitledFormTag {
    private static final long serialVersionUID = 8049519129092850184L;
    private String type;

    @Override
    protected String getTitle() {
        if (UpdateExecutorDetailsForm.TYPE_ACTOR.equals(type)) {
            return MessagesExecutor.TITLE_CREATE_ACTOR.message(pageContext);
        } else {
            return MessagesExecutor.TITLE_CREATE_GROUP.message(pageContext);
        }
    }

    public String getType() {
        return type;
    }

    @Attribute(required = true)
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void fillFormElement(TD tdFormElement) {
        Delegates.getAuthorizationService().checkAllowed(getUser(), Permission.CREATE_EXECUTOR, SecuredSingleton.SYSTEM);
        boolean isActor = UpdateExecutorDetailsForm.TYPE_ACTOR.equals(type);
        ExecutorTableBuilder builder = new ExecutorTableBuilder(isActor, pageContext);
        tdFormElement.addElement(builder.buildTable());
        tdFormElement.addElement(createHiddenType());
    }

    private Input createHiddenType() {
        return new Input(Input.HIDDEN, UpdateExecutorDetailsForm.EXECUTOR_TYPE_INPUT_NAME, type);
    }

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_APPLY.message(pageContext);
    }

    @Override
    public String getAction() {
        return CreateExecutorAction.ACTION_PATH;
    }
}
