package ru.runa.af.web.tag;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.UpdateExecutorDetailsAction;
import ru.runa.af.web.html.ExecutorTableBuilder;
import ru.runa.common.web.MessagesCommon;
import ru.runa.wfe.user.SystemExecutors;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "updateExecutorDetailsForm")
public class UpdateExecutorDetailsFormTag extends UpdateExecutorBaseFormTag {
    private static final long serialVersionUID = 9096797376521541558L;

    @Override
    public void fillFormData(TD tdFormElement) {
        boolean isCheckboxInputDisabled = !isSubmitButtonEnabled();
        ExecutorTableBuilder builder = new ExecutorTableBuilder(getExecutor(), isCheckboxInputDisabled, pageContext);
        tdFormElement.addElement(builder.buildTable());
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        if (SystemExecutors.PROCESS_STARTER_NAME.equals(getExecutor().getName())) {
            return false;
        }
        return super.isSubmitButtonEnabled();
    }

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_APPLY.message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_EXECUTOR_DETAILS.message(pageContext);
    }

    @Override
    public String getAction() {
        return UpdateExecutorDetailsAction.ACTION_PATH;
    }
}
