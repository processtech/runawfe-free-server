package ru.runa.wf.web.tag;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.WebResources;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.TaskFormBuilder;
import ru.runa.wf.web.TaskFormBuilderFactory;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "startForm")
public class StartFormTag extends WFFormTag {
    private static final long serialVersionUID = -1162637745236395968L;
    private Long definitionId;

    @Override
    protected Long getDefinitionId() {
        return definitionId;
    }

    @Attribute(required = true)
    public void setDefinitionId(Long definitionId) {
        this.definitionId = definitionId;
    }

    @Override
    protected String buildForm(Interaction interaction) {
        TaskFormBuilder startFormBuilder = TaskFormBuilderFactory.createTaskFormBuilder(getUser(), pageContext, interaction);
        return startFormBuilder.build(getDefinitionId());
    }

    @Override
    protected Interaction getInteraction() {
        return Delegates.getDefinitionService().getStartInteraction(getUser(), definitionId);
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        super.fillFormElement(tdFormElement);
        tdFormElement.addElement(new Input(Input.HIDDEN, IdForm.ID_INPUT_NAME, String.valueOf(definitionId)));
    }

    @Override
    protected String getSubmitButtonName() {
        String processStartButtonName = WebResources.getButtonName("process.processStartButtonName");

        return processStartButtonName != null ? processStartButtonName :
                MessagesProcesses.LABEL_START_PROCESS.message(pageContext);
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.START_PROCESS_PARAMETER;
    }
}
