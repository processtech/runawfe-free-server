package ru.runa.wf.web.tag;

import java.util.List;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Label;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.WebResources;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.UpdateProcessVariableAction;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.var.VariableDefinition;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "updateProcessVariables")
public class UpdateProcessVariablesFormTag extends TitledFormTag {

    private static final long serialVersionUID = 1L;

    private Long processId;

    @Attribute(required = false, rtexprvalue = true)
    public void setProcessId(Long id) {
        processId = id;
    }

    public Long getProcessId() {
        return processId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        WfProcess process = Delegates.getExecutionService().getProcess(getUser(), getProcessId());
        List<VariableDefinition> variables = getVariableDefinitions(process.getDefinitionId());
        if (!variables.isEmpty()) {
            if (WebResources.isUpdateProcessVariablesEnabled() && isAvailable()) {
                getForm().setEncType(Form.ENC_UPLOAD);
                String labelTDWidth = "150px";

                Table table = new Table();
                table.setClass(Resources.CLASS_LIST_TABLE);
                tdFormElement.addElement(table);

                Input searchInput = HTMLUtils.createInput("searchVariable","");
                searchInput.addAttribute("autocomplete", "off");
                table.addElement(HTMLUtils.createRow(MessagesProcesses.LABEL_SEARCH_VARIABLE.message(pageContext), searchInput));
                TR variableInputTr = new TR();
                variableInputTr.setClass("variableInputRegion");

                TD labelTd = new TD();
                Label labelInputValue = new Label("variableInput");
                labelInputValue.addElement(MessagesProcesses.LABEL_VARIABLE_NEW_VALUE.message(pageContext) + ":&nbsp;");
                labelTd.addElement(labelInputValue);
                labelTd.setWidth(labelTDWidth);
                variableInputTr.addElement(labelTd.setClass(Resources.CLASS_LIST_TABLE_TD));

                TD inputTd = new TD();
                Div inputDiv = new Div();
                inputDiv.setID("variableInput");
                inputTd.addElement(inputDiv);

                Input nullValue = new Input(Input.CHECKBOX, "isNullValue");
                nullValue.setID("nullValueCheckbox");
                nullValue.setChecked(false);
                inputTd.addElement(nullValue);

                Label labelNullValue = new Label("isNullValue");
                labelNullValue.setID("nullValueLabel");
                labelNullValue.addElement(MessagesProcesses.LABEL_VARIABLE_NULL_VALUE.message(pageContext) + "&nbsp;");
                inputTd.addElement(labelNullValue);

                variableInputTr.addElement(inputTd.setClass(Resources.CLASS_LIST_TABLE_TD));
                table.addElement(variableInputTr);
            }
        } else {
            Label variablesExist = new Label("variables");
            variablesExist.addElement(MessagesProcesses.LABEL_NO_VARIABLES.message(pageContext) + "&nbsp;");
            tdFormElement.addElement(variablesExist);
        }
    }

    protected List<VariableDefinition> getVariableDefinitions(Long processDefinitionId) {
        return Delegates.getDefinitionService().getVariableDefinitions(getUser(), processDefinitionId);
    }

    protected boolean isAvailable() {
        return Delegates.getExecutorService().isAdministrator(getUser());
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.BUTTON_UPDATE_VARIABLE.message(pageContext);
    }

    @Override
    public String getAction() {
        return UpdateProcessVariableAction.ACTION_PATH + "?id=" + processId;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesProcesses.BUTTON_UPDATE_VARIABLE.message(pageContext);
    }
    @Override
    protected boolean isCancelButtonEnabled() {
        return true;
    }

    @Override
    protected String getCancelButtonAction() {
        return "manage_process.do?id=" + getProcessId();
    }

}
